package dev.zerojdk.domain.port.out.config;

import java.util.Optional;

public interface ConfigRepository {
    Optional<String> readVersion(boolean global);
    void writeVersion(boolean global, String version);
}
