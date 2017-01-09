package com.vevo.upsilon.store;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class VersionSerializerTest {

    @Test
    public void serialize() {
        assertEquals(VersionSerializer.serialize(Version.from("1.0a")), "1.0a");
    }

    @Test
    public void deserialize() {
        assertEquals(VersionSerializer.deserialize("1.0a"), Version.from("1.0a"));
    }

}