package com.vevo.upsilon.store;

public class VersionSerializer {

    public static String serialize(Version version) {
        return version.getId();
    }

    public static Version deserialize(String serialized) {
        return Version.from(serialized);
    }

    public static String sanitizeName(String name) {
        return "upsilon-"
                + name
                .replaceAll("(?U)\\W+", "")
                .toLowerCase()
                + "-version";
    }
}
