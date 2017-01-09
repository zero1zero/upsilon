package com.vevo.upsilon.lock.filesystem;

import com.vevo.upsilon.lock.Lock;

public class FileSystemLock implements Lock {

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public void unlock() {

    }
}
