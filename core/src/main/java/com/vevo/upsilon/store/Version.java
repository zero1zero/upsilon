package com.vevo.upsilon.store;

public class Version {

    private String id;

    public static Version from(String version) {
        return new Version(version);
    }

    private Version(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Version version1 = (Version) o;

        return id != null ? id.equals(version1.id) : version1.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Version{" +
                "id='" + id + '\'' +
                '}';
    }
}
