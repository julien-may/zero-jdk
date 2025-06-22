package dev.zerojdk.domain.port.out.index;

import dev.zerojdk.domain.model.IndexEntry;
import dev.zerojdk.domain.model.JdkVersion;

import java.nio.file.Path;
import java.util.Optional;

public interface RegistrationRepository {
    // TODO: Use IndexEntry
    void register(JdkVersion jdkVersion, Path releaseRoot, Path javaHome);
    Optional<IndexEntry> find(String identifier);
}
