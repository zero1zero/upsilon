package com.vevo.upsilon.task.load;

import org.testng.annotations.Test;

public class ClasspathTaskLoaderTest {

    @Test
    public void loadSimple() {
        ClasspathTasksLoader.from("/tasks.up");
    }

}