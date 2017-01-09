package com.vevo.upsilon.store;

import java.util.Optional;

public interface Store {

    Optional<Version> getVersion();

    void setVersion(Version version);
}
