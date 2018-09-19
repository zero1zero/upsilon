package com.vevo.upsilon;

import org.testng.annotations.Test;

public class BuilderTest {

    /**
     * Builder make work to test thing
     */
    @Test
    public void defaultBuild() {
        Upsilon upsilon = Upsilon.newBuilder()
                .build();

        upsilon.upgrade();

    }
}