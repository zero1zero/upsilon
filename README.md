![logo.up](upsilon.jpg)

# Upsilon
Upgrade all the things

## Purpose
Upsilon is a framework for migrating and upgrading production services in a cluster.

In an environment with multiple, horizontally scalable services, we often
need to be able to run complex upgrade tasks along with upgrading the version
of the service currently running.

Examples of this would be:
* Updating a database schema along with a version change
* Migrating persisted data to fit a new required data format
* Running data quality checks after an upgrade
* Backfilling data

Upsilon provides a means to lock your cluster during a version update,
execute any associated tasks with that version, then release the lock,
allowing for rest of the nodes to roll out the new update.

This flow assumes that you have shared data stores amongst the cluster,
software versions that support N-1 versions of their persisted data, and
cluster management software (such as Kubernetes).

## Flow
Typically, clusters are upgraded by rolling a new version of software out to
each node at a time.  Nodes typically have at least two initial states: *UP* and *READY*.
*UP* indicates the node is online, but not ready for production traffic.  *READY* indicates it is
a healthy node ready to recieve traffic.

A canary node will be upgraded with a new version, let's say, upgraded to 1.1 from 1.0.
This node will typically be up and running in the cluster before it is given production traffic.
In this state, the *UP* state, is when an Upsilon upgrade should take place.

Once Upsilon has completed it's execution, the node can be set to a *READY* state, and begin recieving production traffic.
The rolling upgrade then can proceed to the rest of the nodes, none of which will have to execute any Upsilon upgrades (for reasons explained later).

## Terminology
#### Task
A task is a unit of work associated with a version of the service's software.
Tasks are defined in a file contained within your app in this form:
```
0.9
  - com.acme.upgrade.BackfillLegacyTask
  - com.acme.upgrade.MigrateDataTask
  - com.acme.upgrade.SchemaUpgradeTask
1.0
  - com.acme.upgrade.ProductionReadyDataCheckTask
```
More detail on tasks later.

#### Lock
Locks are how your canary node can indicate to other nodes in the cluster that it
has control of the upgrade process.  There are multiple implementations of a locking mechanism you
can choose from, or write your own!  The essense of the locking mechanism is a persistant store that
can be utilized to store transient lock information.

#### Store
The store is how your service nodes know what version they currently are.  Nodes are generally aware
of their software version, but not the version of their backing data.  The store provides a place
to tell the cluster how far we have upgraded our shared persistant storage.

It is through this mechanism that once the canary node has executed our upgrade tasks, none of the other
nodes in the cluster have to do the same work multiple times.

## Usage
### Upsilon
For the purposes of example usage, we are going to assume you have a health check
endpoint with a simple `ready()` method.

An example of very basic, single node, usage:

```java
public class HealthCheck {

    /**
     * Called to determine if the service node is ready to receive production traffic
     */
    boolean ready() {
        //default Upsilon setup, pulling tasks from 'tasks.up'
        //locks and storage will use in memory implementations, meaning no versions will be persisted
        Upsilon upsilon = Upsilon.newBuilder().build();

        //will block the thread until the tasks are all processed under
        upsilon.upgrade().get();
    }
}
```

A more complex example using S3 as the lock and version store implementation.

```java
public class HealthCheck {

    boolean ready() {
        StaticCredentialsProvider provider = new StaticCredentialsProvider(
                                                new AwsCredentials("<key>", "<secret>"));

        //lets store our locks and versions in s3 as a means of persistance
        S3Store store = new S3Store(provider, "abc-company-services-bucket", "myservice.version");
        S3Lock lock = new S3Lock(provider, "abc-company-services-bucket", "myservice.lock");

        //default Upsilon setup, pulling tasks from 'tasks.up'
        Upsilon upsilon = Upsilon.newBuilder()
                            .lock(lock)
                            .store(store)
                            .build();

        //this will lock the service using s3, then run the upgrade tasks, updating the version in s3 to the newest
        upsilon.upgrade().get();
    }
}
```

### Tasks
Tasks define the unit of work to be processed before a service can be upgraded
to its next version. Every task is an implementation of `com.vevo.upsilon.task.Task`.

Task objects have two methods: `upgrade` and `rollback`.

The `upgrade` method returns _only_ when the upgrade task has finished
its work.

In the event of a failure, the `rollback` method is called to undo
any work that the upgrade task has done.

*Both methods should be idempotent*.

### Dependencies
When tasks require outside dependencies, they may use the `@Inject` annotation
on the tasks constructor, to automatically load a constructor argument dependency.

Injected dependencies _MUST_ be registered using the `Upsilon` builder method `dependencies`,
with a `com.vevo.upsilon.di.Dependencies` object passed as an argument.  For example:

```

Upsilon upsilon = Upsilon.newBuilder()
    .register(new Dependencies() {

        @Override
        public void configure() {
            bind(new MySQLHelper());
            bind(new MyShinyDep());
        }
    })
    .build();
```

A task that utilizes these dependencies might look something like this:
```
public class DataMigrationTask implements Task {

    private final MySQLHelper sqlHelper;
    private final MyShinyDep shinyDep;

    @Inject
    public DataMigrationTask(MySQLHelper sqlHelper, MyShinyDep shinyDep) {
        this.sqlHelper = sqlHelper;
        this.shinyDep = shinyDep;
    }

    @Override
    public void upgrade() {
        //work
    }

    @Override
    public void rollback() {
        //rollback
    }

}
```