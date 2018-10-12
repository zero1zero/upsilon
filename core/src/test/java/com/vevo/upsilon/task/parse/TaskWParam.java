package com.vevo.upsilon.task.parse;

import com.vevo.upsilon.task.Task;

import javax.inject.Inject;

public class TaskWParam implements Task {

    private final String thing;
    private final String hello;

    @Inject
    public TaskWParam(String thing, String hello) {
        this.thing = thing;
        this.hello = hello;
    }

    @Override
    public void upgrade() throws Exception {

    }

    @Override
    public void rollback() throws Exception {

    }

    public String getThing() {
        return thing;
    }

    public String getHello() {
        return hello;
    }
}
