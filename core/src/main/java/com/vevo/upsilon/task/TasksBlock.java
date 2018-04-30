package com.vevo.upsilon.task;

import com.vevo.upsilon.store.Version;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@EqualsAndHashCode
@ToString
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
}
