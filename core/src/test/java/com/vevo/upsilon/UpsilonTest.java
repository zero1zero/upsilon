package com.vevo.upsilon;

import com.vevo.upsilon.lock.Lock;
import com.vevo.upsilon.store.Store;
import com.vevo.upsilon.store.Version;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UpsilonTest {
    @Mock private Lock lock;
    @Mock private Store store;

    private Upsilon upsilon;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        upsilon = Upsilon.newBuilder()
                .store(store)
                .lock(lock).build();
    }

    @BeforeMethod
    public void reset() {
        Mockito.reset(lock, store);

        when(store.getVersion()).thenReturn(Optional.empty());
        when(lock.tryLock()).thenReturn(true);
    }

    @Test
    public void withoutLockAndVersionUpgrades() throws Exception {
        upsilon.upgrade().get();

        verify(lock).tryLock();
        verify(store, times(2)).getVersion();

        //make sure the version set at end is our only version
        verify(store).setVersion(eq(Version.from("0.1a")));
    }

    @Test
    public void withLockAndOutOfDateUpgradesPolls() throws Exception {
        when(store.getVersion()).thenReturn(Optional.of(Version.from("0.1a")));

        upsilon.upgrade().get();

        verify(lock, never()).tryLock();
        verify(store, never()).setVersion(any());
    }
}