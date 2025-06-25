package dev.zerojdk.domain.port.out.config;

public interface JdkConfigRepository {
    String readVersion(boolean global);
    void update(boolean global, String version);
    void create(boolean global, String version);
}
