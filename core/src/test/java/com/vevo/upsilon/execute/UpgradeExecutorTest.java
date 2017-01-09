package com.vevo.upsilon.execute;

import com.beust.jcommander.internal.Lists;
import com.vevo.upsilon.lock.Lock;
import com.vevo.upsilon.store.Store;
import com.vevo.upsilon.store.Version;
import com.vevo.upsilon.task.TasksBlock;
import com.vevo.upsilon.task.TasksHolder;
import com.vevo.upsilon.task.TasksHolderTest;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Optional;

import static com.vevo.upsilon.task.TasksHolderTest.mockTasks;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class UpgradeExecutorTest {

    /**
     * If there is no initial version, lets be sure it completes
     */
    @Test
    public void noInitialVersion() {
        Store store = mock(Store.class);
        when(store.getVersion()).thenReturn(Optional.empty());

        TasksBlock tasksBlock = mock(TasksBlock.class);
        when(tasksBlock.getVersion()).thenReturn(Version.from("whatever"));

        TasksHolder tasksHolder = mock(TasksHolder.class);
        when(tasksHolder.getTasksBlocks()).thenReturn(Lists.newArrayList(tasksBlock));

        UpgradeExecutor executor = UpgradeExecutor.create(store, tasksHolder);
        executor.execute();

        verify(store).getVersion();
        verify(tasksHolder).getTasksBlocks();

        //make sure the version set at end is our only version
        verify(store).setVersion(eq(Version.from("whatever")));
    }

    @Test
    public void initialVersion() {
        Store store = mock(Store.class);
        when(store.getVersion()).thenReturn(Optional.of(Version.from("1.0")));

        TasksBlock tasksBlock1 = mockTasks("1.0", TasksHolderTest.T1.class, TasksHolderTest.T2.class, TasksHolderTest.T3.class);
        TasksBlock tasksBlock2 = mockTasks("1.1", TasksHolderTest.T4.class, TasksHolderTest.T5.class);

        TasksHolder tasksHolder = new TasksHolder(Lists.newArrayList(tasksBlock1, tasksBlock2));

        UpgradeExecutor executor = UpgradeExecutor.create(store, tasksHolder);
        executor.execute();

        verify(store).getVersion();

        //upgrade next version
        assertTrue(((TasksHolderTest.T4)tasksBlock2.getTasks().get(0)).upgraded);
        assertTrue(((TasksHolderTest.T5)tasksBlock2.getTasks().get(1)).upgraded);

        //make sure the version set at end is our only version
        verify(store).setVersion(eq(Version.from("1.1")));
    }

    @Test
    public void multiblocks() {
        Store store = mock(Store.class);
        when(store.getVersion()).thenReturn(Optional.empty());

        TasksBlock tasksBlock1 = mockTasks("1.0", TasksHolderTest.T1.class, TasksHolderTest.T2.class, TasksHolderTest.T3.class);
        TasksBlock tasksBlock2 = mockTasks("1.1", TasksHolderTest.T4.class, TasksHolderTest.T5.class);

        TasksHolder tasksHolder = new TasksHolder(Lists.newArrayList(tasksBlock1, tasksBlock2));

        UpgradeExecutor executor = UpgradeExecutor.create(store, tasksHolder);
        executor.execute();

        verify(store).getVersion();

        assertTrue(((TasksHolderTest.T1)tasksBlock1.getTasks().get(0)).upgraded);
        assertTrue(((TasksHolderTest.T2)tasksBlock1.getTasks().get(1)).upgraded);
        assertTrue(((TasksHolderTest.T3)tasksBlock1.getTasks().get(2)).upgraded);
        assertTrue(((TasksHolderTest.T4)tasksBlock2.getTasks().get(0)).upgraded);
        assertTrue(((TasksHolderTest.T5)tasksBlock2.getTasks().get(1)).upgraded);
    }

    @Test
    public void failedTask() {
        Store store = mock(Store.class);
        when(store.getVersion()).thenReturn(Optional.empty());

        TasksBlock tasksBlock1 = mockTasks("1.0", TasksHolderTest.T1.class, TasksHolderTest.T2.class, TasksHolderTest.T3.class);
        TasksBlock tasksBlock2 = mockTasks("1.1", TasksHolderTest.T4.class);

        TasksHolder tasksHolder = new TasksHolder(Lists.newArrayList(tasksBlock1, tasksBlock2));

        //fail the T2 task
        ((TasksHolderTest.T2)tasksBlock1.getTasks().get(1)).failUpgrade = true;

        UpgradeExecutor executor = UpgradeExecutor.create(store, tasksHolder);
        executor.execute();

        verify(store).getVersion();

        //since this task failed,
        assertTrue(((TasksHolderTest.T1)tasksBlock1.getTasks().get(0)).rolledback);
        assertTrue(((TasksHolderTest.T2)tasksBlock1.getTasks().get(1)).rolledback);

        //never attempted upgrade
        assertFalse(((TasksHolderTest.T3)tasksBlock1.getTasks().get(2)).upgraded);
        assertFalse(((TasksHolderTest.T4)tasksBlock2.getTasks().get(0)).upgraded);
    }
}