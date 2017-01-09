package com.vevo.upsilon.store;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class FileSystemStoreTest {

    /**
     * Let's make sure the version files look OK for all languages
     */
    @Test
    public void sanitizeName() {
        String sanitized = VersionSerializer.sanitizeName("What's up wiT W3rd Versions は最高でし    <-- invis chars");

        assertEquals(sanitized, "upsilon-whatsupwitw3rdversionsは最高でしinvischars-version");
    }

}