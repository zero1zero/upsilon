package com.vevo.upsilon.task;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.vevo.upsilon.di.DependencyInjector;
import com.vevo.upsilon.except.UpsilonInitializationException;
import com.vevo.upsilon.store.Version;
import com.vevo.upsilon.task.parse.ParsedVersion;
import com.vevo.upsilon.task.parse.ParsedVersions;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

public class TasksHolderTest {

    /**
     * Make sure we can load our task classes fine
     */
    @Test
    public void testLoadTaskClass() {
        ParsedVersion version = mock(ParsedVersion.class);
        when(version.getVersion()).thenReturn("1.1");
        when(version.getTasks()).thenReturn(Lists.newArrayList("com.vevo.upsilon.task.parse.DummyTask"));

        ParsedVersions versions = mock(ParsedVersions.class);
        when(versions.getVersions()).thenReturn(Lists.newArrayList(version));

        DependencyInjector injector = new DependencyInjector();

        TasksHolder store = TasksHolder.load(versions, injector);

        assertNotNull(store.getTasksBlocks().iterator().next());
    }

    /**
     * If the task class cannot be instantiated, fail
     */
    @Test(expectedExceptions = UpsilonInitializationException.class, expectedExceptionsMessageRegExp = ".*not found on the classpath.*")
    public void testLoadFailTaskClass() {
        ParsedVersion version = mock(ParsedVersion.class);
        when(version.getVersion()).thenReturn("1.1");
        when(version.getTasks()).thenReturn(Lists.newArrayList("com.vevo.upsilon.task.parse.IDontExist"));

        ParsedVersions versions = mock(ParsedVersions.class);
        when(versions.getVersions()).thenReturn(Lists.newArrayList(version));

        DependencyInjector injector = new DependencyInjector();

        TasksHolder.load(versions, injector);
    }

    public static class TaskWithDep implements Task {

        Multimap dep;

        @Inject
        public TaskWithDep(Multimap dep) {
            this.dep = dep;
        }

        @Override
        public void upgrade() {}

        @Override
        public void rollback() {}
    }

    /**
     * Let's make sure that we can use a task with a module dependency injected
     */
    @Test
    public void loadClassWithDep() {
        ParsedVersion version = mock(ParsedVersion.class);
        when(version.getVersion()).thenReturn("1.1");
        when(version.getTasks()).thenReturn(Lists.newArrayList("com.vevo.upsilon.task.TasksHolderTest$TaskWithDep"));

        ParsedVersions versions = mock(ParsedVersions.class);
        when(versions.getVersions()).thenReturn(Lists.newArrayList(version));

        DependencyInjector injector = new DependencyInjector();
        injector.register(ArrayListMultimap.create(), Multimap.class);

        TasksHolder store = TasksHolder.load(versions, injector);
        TasksBlock tasks = store.getTasksBlocks().iterator().next();

        assertNotNull(((TaskWithDep) tasks.getTasks().get(0)).dep);
    }

    /**
     * Let's be sure that getting the next version works as expected
     */
    @Test
    public void tasksToRun() {
        TasksBlock onePoint0= mock(TasksBlock.class);
        when(onePoint0.getVersion()).thenReturn(Version.from("1.0"));

        TasksBlock onePoint1= mock(TasksBlock.class);
        when(onePoint1.getVersion()).thenReturn(Version.from("1.1"));

        TasksBlock onePoint2= mock(TasksBlock.class);
        when(onePoint2.getVersion()).thenReturn(Version.from("1.2"));

        TasksBlock onePoint3= mock(TasksBlock.class);
        when(onePoint3.getVersion()).thenReturn(Version.from("1.3"));

        TasksHolder store = new TasksHolder(Lists.newArrayList(Lists.newArrayList(onePoint0, onePoint1, onePoint2, onePoint3)));

        Iterable<TasksBlock> restOfTasks = store.getTasksBlocksAfter(Version.from("1.1"));

        //getting the versions after 1.1 should yield everything after 1.1 and 1.2
        assertEquals(Iterables.get(restOfTasks, 0).getVersion().getId(), "1.2");
        assertEquals(Iterables.get(restOfTasks, 1).getVersion().getId(), "1.3");
    }

    public static TasksBlock mockTasks(String id, Class<? extends Task> ... tasks) {
        TasksBlock tb= mock(TasksBlock.class);
        when(tb.getVersion()).thenReturn(Version.from(id));
        when(tb.getTasks()).thenReturn(Lists.newArrayList(tasks)
                .stream()
                .map(taskClass -> {
                    try {
                        return taskClass.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        fail("Can't instantiate task class", e);
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList()));

        return tb;
    }

    static class AT implements Task {

        public boolean upgraded = false;
        public boolean rolledback = false;

        public boolean failUpgrade = false;
        public boolean failRollback = false;

        public AT() {}

        @Override
        public void upgrade() {
            if (failUpgrade) {
                throw new RuntimeException();
            }
            upgraded = true;
        }

        @Override
        public void rollback() {
            if (failRollback) {
                throw new RuntimeException();
            }
            rolledback = true;
        }

        @Override
        public boolean equals(Object obj) {
            return obj.getClass() == getClass();
        }


    }
    public static class T1 extends AT {public T1() {}}
    public static class T2 extends AT {public T2() {}}
    public static class T3 extends AT {public T3() {}}
    public static class T4 extends AT {public T4() {}}
    public static class T5 extends AT {public T5() {}}
    public static class T6 extends AT {public T6() {}}
    public static class T7 extends AT {public T7() {}}
    public static class T8 extends AT {public T8() {}}
    public static class T9 extends AT {public T9() {}}
    public static class T10 extends AT {public T10() {}}
    public static class T11 extends AT {public T11() {}}
    public static class T12 extends AT {public T12() {}}

    /**
     * Getting tasks in reverse order back from a particular point in a version.
     *
     * I.e. when T6 fails, we should return all the previous tasks (inclusively) that were before it in the task (in order).
     * T6, T5, T4, T3
     *
     */
    @Test
    public void getTasksBefore() throws IllegalAccessException, InstantiationException {
        TasksBlock one = mockTasks("1.0", T1.class, T2.class);
        TasksBlock onepoint1 = mockTasks("1.1", T3.class, T4.class, T5.class, T6.class, T7.class);
        TasksBlock onepoint2 = mockTasks("1.2", T8.class, T9.class);
        TasksBlock onepoint3 = mockTasks("1.3", T10.class, T11.class, T12.class);

        TasksHolder store = new TasksHolder(Lists.newArrayList(Lists.newArrayList(one, onepoint1, onepoint2, onepoint3)));
        Iterable<Task> finalTasks = store.getTasksBefore(Version.from("1.1"), T6.class.newInstance());

        assertEquals(Iterables.get(finalTasks, 0).getClass(), T6.class);
        assertEquals(Iterables.get(finalTasks, 1).getClass(), T5.class);
        assertEquals(Iterables.get(finalTasks, 2).getClass(), T4.class);
        assertEquals(Iterables.get(finalTasks, 3).getClass(), T3.class);
    }

    /**
     * Another permutation of getting tasks
     */
    @Test
    public void getTasksBefore1() throws IllegalAccessException, InstantiationException {
        TasksBlock one = mockTasks("1.0", T1.class, T2.class);
        TasksBlock onepoint1 = mockTasks("1.1", T3.class, T4.class, T5.class, T6.class, T7.class);
        TasksBlock onepoint2 = mockTasks("1.2", T8.class, T9.class);
        TasksBlock onepoint3 = mockTasks("1.3", T10.class, T11.class, T12.class);

        TasksHolder store = new TasksHolder(Lists.newArrayList(Lists.newArrayList(one, onepoint1, onepoint2, onepoint3)));
        Iterable<Task> finalTasks = store.getTasksBefore(Version.from("1.2"), T8.class.newInstance());

        assertEquals(Iterables.get(finalTasks, 0).getClass(), T8.class);
    }

    @Test
    public void getTasksBefore2() throws IllegalAccessException, InstantiationException {
        TasksBlock one = mockTasks("1.0", T1.class, T2.class);
        TasksBlock onepoint1 = mockTasks("1.1", T3.class, T4.class, T5.class, T6.class, T7.class);
        TasksBlock onepoint2 = mockTasks("1.2", T8.class, T9.class);
        TasksBlock onepoint3 = mockTasks("1.3", T10.class, T11.class, T12.class);

        TasksHolder store = new TasksHolder(Lists.newArrayList(Lists.newArrayList(one, onepoint1, onepoint2, onepoint3)));
        Iterable<Task> finalTasks = store.getTasksBefore(Version.from("1.0"), T2.class.newInstance());

        assertEquals(Iterables.get(finalTasks, 0).getClass(), T2.class);
        assertEquals(Iterables.get(finalTasks, 1).getClass(), T1.class);
    }
}