package com.vevo.upsilon.task;

public interface Task {

    void upgrade() throws Throwable;

    void rollback() throws Throwable;
}
