package com.vevo.upsilon.task.parse;

import com.vevo.upsilon.task.Task;

public class DummyTask implements Task {
    @Override
    public void upgrade() {}

    @Override
    public void rollback() {}
}
