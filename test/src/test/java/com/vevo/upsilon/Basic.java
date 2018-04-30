package com.vevo.upsilon;

import com.vevo.upsilon.execute.UpgradeStatus;
import com.vevo.upsilon.task.Task;
import com.vevo.upsilon.task.load.ClasspathTasksLoader;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutionException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class Basic {

    public static class Task1 implements Task {
        static boolean upgraded = false;
        @Override
        public void upgrade() {
            upgraded = true;
        }

        @Override
        public void rollback() {
            upgraded = false;
        }
    }

    @Test
    public void simpleBuild() throws ExecutionException, InterruptedException {
        Upsilon upsilon = Upsilon.newBuilder()
                .tasksLoader(ClasspathTasksLoader.from("simple-build.up"))
                .build();

        //just make sure it doesnt fail
        upsilon.upgrade().get();

        assertEquals(upsilon.getStatus(), UpgradeStatus.COMPLETED);
        assertTrue(Task1.upgraded);
    }
}
