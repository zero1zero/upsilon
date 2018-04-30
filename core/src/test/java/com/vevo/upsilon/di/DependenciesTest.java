package com.vevo.upsilon.di;

import com.vevo.upsilon.Upsilon;
import com.vevo.upsilon.execute.UpgradeStatus;
import com.vevo.upsilon.task.Task;
import com.vevo.upsilon.task.load.ClasspathTasksLoader;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;

import static java.util.Objects.requireNonNull;
import static org.testng.Assert.assertEquals;

public class DependenciesTest {

    public static class Dep1 {}

    public static class TaskWDep implements Task {
        @Inject
        public TaskWDep(Dep1 dep1) {
            requireNonNull(dep1);
        }

        @Override
        public void upgrade() {

        }

        @Override
        public void rollback() {

        }
    }

    /**
     * Load a basic task with a dependency and make sure our module provides it
     */
    @Test
    public void taskWithDep() throws ExecutionException, InterruptedException {
        Upsilon upsilon = Upsilon.newBuilder()
                .register(new Dependencies() {

                    @Override
                    public void configure() {
                        bind(new Dep1());
                    }
                })
                .tasksLoader(ClasspathTasksLoader.from("task-w-dep.up"))
                .build();

        //just make sure it doesnt fail
        upsilon.upgrade().get();

        assertEquals(upsilon.getStatus(), UpgradeStatus.COMPLETED);
    }
}
