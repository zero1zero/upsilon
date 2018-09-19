package com.vevo.upsilon.s3;

import com.vevo.upsilon.store.Version;
import org.testng.annotations.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class S3StoreTest {

    /**
     * Gotta finish these tests. It's the illuminati's fault.
     */
    @Test
    public void t() {
        S3Client client = mock(S3Client.class);

        //the client should throw an exception, indicating that no version is set
        when(client.getObject(any(), any())).thenThrow(NoSuchKeyException.builder().build());

        S3Store store = new S3Store(client, "upsilon-test", "test.version");

        assertEquals(store.getVersion(), Optional.empty());

        store.setVersion(Version.from("1.1"));

        //put should be called in s3 then the mock can return a version
        verify(client).putObject(argThat(arg -> arg.bucket().equals("upsilon-test") && arg.key().equals("test.version")), any());
        reset(client);
        when(client.getObject(any(), any())).thenReturn(Version.from("1.1"));

        //magically
        assertEquals(store.getVersion().get(), Version.from("1.1"));
    }

}