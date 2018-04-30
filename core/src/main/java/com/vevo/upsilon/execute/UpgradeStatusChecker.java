package com.vevo.upsilon.execute;

import com.vevo.upsilon.lock.Lock;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class UpgradeStatusChecker {

    private Lock lock;

    public UpgradeStatusChecker(Lock lock) {
        this.lock = lock;
    }

    public CompletableFuture<Void> waitForUnlock() {
        return CompletableFuture.runAsync(() -> {
            try {
                boolean gotLock;
                do {
                    gotLock = lock.tryLock();

                    Thread.sleep(1000);
                } while (!gotLock);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
