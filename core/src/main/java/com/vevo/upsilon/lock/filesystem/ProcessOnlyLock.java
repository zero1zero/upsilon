package com.vevo.upsilon.lock.filesystem;

import com.vevo.upsilon.lock.Lock;

import java.util.concurrent.Semaphore;

public class ProcessOnlyLock implements Lock {

    private final Semaphore lock = new Semaphore(1);

    @Override
    public boolean tryLock() {
        return lock.tryAcquire();
    }

    @Override
    public void unlock() {
        lock.release();
    }
}
