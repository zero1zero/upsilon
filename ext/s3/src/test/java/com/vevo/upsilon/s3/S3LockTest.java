package com.vevo.upsilon.s3;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.sync.StreamingResponseHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

public class S3LockTest {

    /**
     * Gotta finish these tests. Leave me alone I'm tired.
     */
    @Test
    public void basicLockUnlock() {

        S3Client client = mock(S3Client.class);

        S3Lock lock = new S3Lock(client, "upsilon-test", "test.lock");
        when(client.getObject(any(), any())).thenReturn(true);



        assertTrue(lock.tryLock());
//        assertFalse(lock.tryLock());
//        lock.unlock();
//        assertTrue(lock.tryLock());
//        lock.unlock();

        //grab the callback in a captor
        ArgumentCaptor<StreamingResponseHandler> argument = ArgumentCaptor.forClass(StreamingResponseHandler.class);
        verify(client).getObject(any(), argument.capture());
    }
}