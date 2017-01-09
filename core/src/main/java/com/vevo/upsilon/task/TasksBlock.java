package com.vevo.upsilon.task;

import com.vevo.upsilon.store.Version;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class TasksBlock {

    private final Version version;
    private final List<Task> tasks;

    TasksBlock(Version version, List<Task> tasks) {
        this.version = checkNotNull(version);
        this.tasks = checkNotNull(tasks);
    }

    public Version getVersion() {
        return version;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TasksBlock that = (TasksBlock) o;

        if (!version.equals(that.version)) return false;
        return tasks.equals(that.tasks);
    }

    @Override
    public int hashCode() {
        int result = version.hashCode();
        result = 31 * result + tasks.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TasksBlock{" +
                "version=" + version +
                ", tasks=" + tasks +
                '}';
    }
}
