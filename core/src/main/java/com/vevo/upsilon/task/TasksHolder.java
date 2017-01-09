package com.vevo.upsilon.task;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.vevo.upsilon.except.UpsilonInitializationException;
import com.vevo.upsilon.except.UpsilonUpgradeException;
import com.vevo.upsilon.store.Version;
import com.vevo.upsilon.task.parse.ParsedVersion;
import com.vevo.upsilon.task.parse.ParsedVersions;
import org.codejargon.feather.Feather;
import org.codejargon.feather.Key;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

public class TasksHolder {

    private final List<TasksBlock> tasksBlocks = Lists.newArrayList();

    public static TasksHolder load(ParsedVersions versions, Feather feather) {

        TasksHolder store = new TasksHolder();

        for (ParsedVersion version : versions.getVersions()) {

            List<Task> tasks = new ArrayList<>(version.getTasks().size());

            for (String task : version.getTasks()) {

                Class<?> taskClass;
                try {
                    taskClass = Class.forName(task);
                } catch (ClassNotFoundException e) {
                    throw new UpsilonInitializationException("Task class '" + task + "' not found on the classpath!", e);
                }

                Task loaded = (Task) feather.instance(Key.of(taskClass));

                checkState(loaded != null, "Could not instantiate task class '" + taskClass + "'");

                tasks.add(loaded);
            }

            store.tasksBlocks.add(new TasksBlock(Version.from(version.getVersion()), tasks));
        }

        return store;
    }

    private TasksHolder() {}

    @VisibleForTesting
    public TasksHolder(List<TasksBlock> tasksBlocks) {
        this.tasksBlocks.addAll(tasksBlocks);
    }

    public Iterable<TasksBlock> getTasksBlocks() {
        return ImmutableList.copyOf(tasksBlocks);
    }

    public Iterable<TasksBlock> getTasksBlocksAfter(Version current) {
        int currentVersion = -1;

        List<TasksBlock> tasks = this.tasksBlocks;
        for (int i = 0; i < tasks.size(); i++) {
            TasksBlock vt = tasks.get(i);

            if (vt.getVersion().equals(current)) {
                currentVersion = i;
            }
        }

        if (currentVersion == -1) {
            throw new UpsilonUpgradeException("Unable to find stored version in declared version and tasks list!");
        }

        //get the next version after current
        return ImmutableList.copyOf(tasksBlocks.subList(currentVersion + 1, tasksBlocks.size()));
    }

    /**
     * Returns a reverse sorted list of tasks at version {@code version} and before (inclusive) of {@code task}
     */
    public Iterable<Task> getTasksBefore(Version version, Task startTask) {

        Optional<TasksBlock> block = tasksBlocks.stream().filter(tb -> tb.getVersion().equals(version)).findFirst();

        if (!block.isPresent()) {
            throw new UpsilonUpgradeException("Can't find version '" + version + "'");
        }

        TasksBlock tb = block.get();

        List<Task> tasks1 = tb.getTasks();
        for (int t1 = 0; t1 < tasks1.size(); t1++) {
            Task task = tasks1.get(t1);
            if (task.equals(startTask)) {

                List<Task> sublist = Lists.newCopyOnWriteArrayList(tb.getTasks().subList(0, t1 + 1));

                Collections.reverse(sublist);

                return ImmutableList.copyOf(sublist);
            }
        }

        throw new UpsilonUpgradeException("Task must exist in the existing task blocks");
    }
}
