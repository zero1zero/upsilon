package com.vevo.upsilon.execute;

import com.vevo.upsilon.lock.Lock;
import com.vevo.upsilon.store.Store;
import com.vevo.upsilon.store.Version;
import com.vevo.upsilon.task.Task;
import com.vevo.upsilon.task.TasksHolder;
import com.vevo.upsilon.task.TasksBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

public class UpgradeExecutor {

    private static final Logger log = LoggerFactory.getLogger(UpgradeExecutor.class);

    private final TasksHolder tasksHolder;
    private final Store store;

    public static UpgradeExecutor create(Store store, TasksHolder tasksHolder) {
        return new UpgradeExecutor(tasksHolder, store);
    }

    private UpgradeExecutor(TasksHolder tasksHolder, Store store) {
        this.tasksHolder = tasksHolder;
        this.store = store;
    }

    public void execute() {

        Iterable<TasksBlock> tasksBlocks;

        Optional<Version> currentVersion = store.getVersion();

        if (currentVersion.isPresent()) {
            tasksBlocks = tasksHolder.getTasksBlocksAfter(currentVersion.get());
        } else {
            log.info("No initial version present");

            tasksBlocks = tasksHolder.getTasksBlocks();

            checkState(tasksBlocks.iterator().hasNext(), "No task blocks available");

            currentVersion = Optional.of(tasksBlocks.iterator().next().getVersion());
        }

        Task failedTask = null;
        for (TasksBlock tasksBlock : tasksBlocks) {
            log.info("Starting upgrade of version task block '" + tasksBlock.getVersion() + "'");

            boolean successfulVersionRun = true;
            for (Task task : tasksBlock.getTasks()) {
                log.info("Running upgrade for task '" + task + "'");
                try {
                    task.upgrade();
                } catch (Throwable t) {

                    log.error("Exception during upgrade task '" + task + "'. Proceeding with rollback of all tasks in that version.", t);

                    successfulVersionRun = false;
                    failedTask = task;
                    break;
                }
            }

            //if one of our tasks failed in the middle of an upgrade, we need to rollback the tasks for that version
            //and keep the version where it is
            if (!successfulVersionRun) {
                checkState(failedTask != null, "Failed task cannot be null on a failed upgrade run");

                //rollback all tasks (including failed) that were executed for this version
                for (Task task : tasksHolder.getTasksBefore(tasksBlock.getVersion(), failedTask)) {
                    log.info("Rolling back '" + task + "'");
                    try {
                        task.rollback();
                    } catch (Throwable e) {
                        log.error("Failure of rollback execution for '" + task + "'. Nothing to do but skip...", e);
                    }
                }

                //set version to last successful
                store.setVersion(currentVersion.get());

                return;
            }

            //if we got here that means that all the tasks completed for this version
            currentVersion = Optional.of(tasksBlock.getVersion());
        }

        //we successfully finished the version run. Set the current version
        store.setVersion(currentVersion.get());

        log.info("Completed upgrade!");
    }
}
