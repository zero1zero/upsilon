package com.vevo.upsilon;

import com.google.common.collect.Lists;
import com.vevo.upsilon.di.Dependencies;
import com.vevo.upsilon.di.DependencyInjector;
import com.vevo.upsilon.execute.UpgradeExecutor;
import com.vevo.upsilon.execute.UpgradeStatus;
import com.vevo.upsilon.execute.UpgradeStatusChecker;
import com.vevo.upsilon.lock.Lock;
import com.vevo.upsilon.lock.filesystem.ProcessOnlyLock;
import com.vevo.upsilon.store.NoopStore;
import com.vevo.upsilon.store.Store;
import com.vevo.upsilon.task.TasksHolder;
import com.vevo.upsilon.task.load.ClasspathTasksLoader;
import com.vevo.upsilon.task.load.TasksLoader;
import com.vevo.upsilon.task.parse.ParsedVersions;
import com.vevo.upsilon.task.parse.ParsedVersionsLoader;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class Upsilon {

    private UpgradeStatus status = UpgradeStatus.INITIALIZING;

    private final Lock lock;
    private final UpgradeExecutor executor;

    public static class Builder {

        private TasksLoader tasksLoader;
        private Store store;
        private Lock lock;
        private List<Dependencies> dependencies = Lists.newArrayList();

        public Builder tasksLoader(TasksLoader tasksLoader) {
            this.tasksLoader = tasksLoader;
            return this;
        }

        public Builder store(Store store) {
            this.store = store;
            return this;
        }

        public Builder lock(Lock lock) {
            this.lock = lock;
            return this;
        }

        public <T extends Dependencies> Builder register(T dependencies) {
            this.dependencies.add(dependencies);
            return this;
        }

        public Upsilon build() {
            if (tasksLoader == null) {
                tasksLoader = ClasspathTasksLoader.from("tasks.up");
            }

            if (lock == null) {
                lock = new ProcessOnlyLock();
                log.warn("No lock implementation specified! Upgrades will only be locked in-process. \n\n" +
                        "Please specify a lock implementation for production.  " +
                        "This gives Upsilon a method to lock your nodes for upgrade");
            }

            if (store == null) {
                store = new NoopStore();

                log.warn("No store implementation specified! Versions will not be persisted! \n\n" +
                        "Please specify a store implementation for production.  " +
                        "This gives Upsilon a method to persist your application version after each upgrade.");
            }

            return new Upsilon(tasksLoader, store, lock, dependencies);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @SuppressWarnings("unchecked")
    private Upsilon(TasksLoader tasksLoader, Store store, Lock lock, List<Dependencies> depBlocks) {
        this.lock = lock;

        DependencyInjector injector = new DependencyInjector();

        for (Dependencies dependencies : depBlocks) {

            dependencies.configure();

            for (Map.Entry<Class<?>, Object> entry : dependencies.getBinds().entrySet()) {
                injector.register(entry.getValue(), (Class) entry.getKey());
            }
        }

        ParsedVersions versions = ParsedVersionsLoader.load(tasksLoader);

        TasksHolder tasksHolder = TasksHolder.load(versions, injector);

        executor = UpgradeExecutor.create(store, tasksHolder);
    }

    public UpgradeStatus getStatus() {
        return status;
    }

    public CompletableFuture<UpgradeStatus> upgrade() {
        status = UpgradeStatus.INITIALIZING;

        return new UpgradeStatusChecker(lock)
                .waitForUnlock()
                .thenRun(() -> status = UpgradeStatus.IN_PROGRESS) //set to running
                .thenCombineAsync(executor.execute(), (aVoid, upgradeStatus) -> upgradeStatus) //run the upgrade!
                .thenApplyAsync(upgradeStatus -> {
                    status = upgradeStatus;

                    //go ahead and release the lock
                    lock.unlock();

                    return upgradeStatus;
                });
    }
}
