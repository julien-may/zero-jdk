package dev.zerojdk.domain.port.out.config;

public interface JdkConfigRepository {
    String readVersion(boolean global);
    void updateVersion(boolean global, String version);
}
