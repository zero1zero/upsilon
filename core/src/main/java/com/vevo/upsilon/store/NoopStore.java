package com.vevo.upsilon.store;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class NoopStore implements Store {

    @Override
    public Optional<Version> getVersion() {
        log.warn("Using NOOP version store! No app version actually retrieved");
        return Optional.empty();
    }

    @Override
    public void setVersion(Version version) {
        log.warn("Setting version {} to the NOOP version store.  No version actually stored", version.getId());
    }
}
