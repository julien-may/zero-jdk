package dev.zerojdk.domain.port.out.config;

import java.util.Optional;

public interface JdkConfigRepository {
    String readVersion(boolean global);
    void writeVersion(boolean global, String version);
}
