package com.vevo.upsilon.lock;

public interface Lock {

    boolean tryLock();

    void unlock();

}
