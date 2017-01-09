package com.vevo.upsilon.store.filesystem;

import com.google.common.base.Charsets;
import com.google.common.base.StandardSystemProperty;
import com.google.common.io.Files;
import com.vevo.upsilon.except.UpsilonUpgradeException;
import com.vevo.upsilon.store.Store;
import com.vevo.upsilon.store.Version;
import com.vevo.upsilon.store.VersionSerializer;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class FileSystemStore implements Store {

    private final File file;

    public static FileSystemStore from(String name) {
        return new FileSystemStore(name);
    }

    private FileSystemStore(String name) {
        String location = StandardSystemProperty.JAVA_IO_TMPDIR.value() + "/" + VersionSerializer.sanitizeName(name);

        this.file = new File(location);

        if (!this.file.canRead()) {
            throw new IllegalStateException("Unable to read from version file '" + location + "'");
        }

        if (!this.file.canWrite()) {
            throw new IllegalStateException("Unable to write to version file '" + location + "'");
        }
    }

    @Override
    public Optional<Version> getVersion() {
        if (!this.file.exists()) {
            return Optional.empty();
        }

        String content;
        try {
            content = Files.toString(file, Charsets.UTF_8);
        } catch (IOException e) {
            throw new UpsilonUpgradeException("There was an issue reading current version", e);
        }

        return Optional.of(VersionSerializer.deserialize(content));
    }

    @Override
    public void setVersion(Version version) {
        String content = VersionSerializer.serialize(version);

        try {
            Files.write(content, this.file, Charsets.UTF_8);
        } catch (IOException e) {
            throw new UpsilonUpgradeException("There was an issue writing current version", e);
        }
    }
}
