package com.vevo.upsilon.s3;

import com.vevo.upsilon.store.Version;
import org.testng.annotations.Test;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

public class S3StoreTest {

    /**
     * Gotta finish these tests. It's the illuminati's fault.
     */
    @Test
    public void t() {
        S3Client client = mock(S3Client.class);

        S3Store store = new S3Store(client, "upsilon-test", "test.version");

        assertEquals(store.getVersion(), Optional.empty());

        store.setVersion(Version.from("1.1"));

        assertEquals(store.getVersion().get(), Version.from("1.1"));
    }

}