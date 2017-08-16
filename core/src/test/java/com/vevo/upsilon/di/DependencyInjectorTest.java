package com.vevo.upsilon.di;

import com.vevo.upsilon.except.UpsilonInitializationException;
import com.vevo.upsilon.task.Task;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.concurrent.Callable;

import static org.testng.Assert.assertNotNull;

public class DependencyInjectorTest {

    public static class Dep {}
    public static class Dep1 {}
    public static class Dep2 {}
    public static class Dep3 implements Callable<String> {
        @Override
        public String call() throws Exception {
            return null;
        }
    }

    public static class ATask implements Task {
        @Override
        public void upgrade() {}
        @Override
        public void rollback() {}
    }

    public static class NoArgsTask extends ATask {}
    public static class OneArgTask extends ATask {
        @Inject
        public OneArgTask(Dep dep) {
            assertNotNull(dep);
        }
    }
    public static class MultiArgTask extends ATask {
        @Inject
        public MultiArgTask(Dep depm, Dep2 dep2, Callable<String> dep3) {
            assertNotNull(depm);
            assertNotNull(dep2);
            assertNotNull(dep3);
        }
    }

    @Test
    public void noArgs() {

        DependencyInjector injector = new DependencyInjector();

        NoArgsTask task = injector.instance(NoArgsTask.class);

        assertNotNull(task);
    }

    @Test(expectedExceptions = UpsilonInitializationException.class, expectedExceptionsMessageRegExp = "^Could not find registered dependency for constructor parameter.*")
    public void oneArgFailNoDep() {

        DependencyInjector injector = new DependencyInjector();

        OneArgTask task = injector.instance(OneArgTask.class);

        assertNotNull(task);
    }

    @Test
    public void oneArg() {

        DependencyInjector injector = new DependencyInjector();
        injector.register(new Dep(), Dep.class);
        injector.register(new Dep1(), Dep1.class);

        OneArgTask task = injector.instance(OneArgTask.class);

        assertNotNull(task);
    }

    @Test
    public void multiArg() {

        DependencyInjector injector = new DependencyInjector();
        injector.register(new Dep(), Dep.class);
        injector.register(new Dep1(), Dep1.class); //not needed but to test extras
        injector.register(new Dep2(), Dep2.class);
        injector.register(new Dep3(), Callable.class); //to test polymorphs

        MultiArgTask task = injector.instance(MultiArgTask.class);

        assertNotNull(task);
    }
}