package com.vevo.upsilon.task.parse;

import org.parboiled.support.Var;

import java.util.ArrayList;
import java.util.List;

public class ParsedVersion {

    private String version;
    private List<String> tasks;

    static Var<ParsedVersion> createVar() {
        return new Var<>(new ParsedVersion());
    }

    ParsedVersion() {}

    public String getVersion() {
        return version;
    }

    public ParsedVersion setVersion(String version) {
        this.version = version;

        return this;
    }

    public List<String> getTasks() {
        return tasks;
    }

    public ParsedVersion addTask(String task) {
        if (this.tasks == null) {
            this.tasks = new ArrayList<>();
        }

        this.tasks.add(task);

        return this;
    }
}
