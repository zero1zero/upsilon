package com.vevo.upsilon;

import com.google.common.collect.Lists;
import com.vevo.upsilon.di.DependencyInjector;
import com.vevo.upsilon.except.UpsilonUpgradeException;
import com.vevo.upsilon.execute.UpgradeExecutor;
import com.vevo.upsilon.lock.Lock;
import com.vevo.upsilon.store.Store;
import com.vevo.upsilon.task.TasksHolder;
import com.vevo.upsilon.task.load.TasksLoader;
import com.vevo.upsilon.task.parse.ParsedVersions;
import com.vevo.upsilon.task.parse.ParsedVersionsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class Upsilon {

    private static final Logger log = LoggerFactory.getLogger(Upsilon.class);

    public static class Builder {

        private TasksLoader tasksLoader;
        private Store store;
        private Lock lock;
        private List<Object> modules;

        public void tasksLoader(TasksLoader tasksLoader) {
            this.tasksLoader = tasksLoader;
        }

        public void store(Store store) {
            this.store = store;
        }

        public void lock(Lock lock) {
            this.lock = lock;
        }

        public <T> void add(T module) {
            if (modules == null) {
                modules = Lists.newArrayList();
            }

            modules.add(module);
        }

        public Upsilon build() {
            checkArgument(tasksLoader != null, "Please add a tasks loader. " +
                    "This tells Upsilon where to find your version tasks file");
            checkArgument(store != null, "Please specify a store implementation.  " +
                    "This gives Upsilon a method to persist your application version");
            checkArgument(lock != null, "Please specify a lock implementation.  " +
                    "This gives Upsilon a method to lock your nodes for upgrade");

            return new Upsilon(tasksLoader, store, lock, modules == null ? Collections.emptyList() : modules);
        }
    }

    private Upsilon(TasksLoader tasksLoader, Store store, Lock lock, List<?> modules) {

        DependencyInjector injector = new DependencyInjector();

        ParsedVersions versions = ParsedVersionsLoader.load(tasksLoader);

        TasksHolder tasksHolder = TasksHolder.load(versions, injector);

        UpgradeExecutor executor = UpgradeExecutor.create(store, tasksHolder);

        if (!lock.tryLock()) {
            throw new UpsilonUpgradeException("Unable to acquire lock for upgrade!");
        }

        executor.execute();

        //go ahead and release the lock
        lock.unlock();
    }
}
