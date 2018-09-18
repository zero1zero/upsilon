package com.vevo.upsilon.task;

public interface Task {

    void upgrade() throws Exception;

    void rollback();
}
