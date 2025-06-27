package dev.zerojdk.domain.port.out.config;

public interface JdkConfigRepository {
    String readVersion(boolean global);
    void update(String version, boolean global);
    void create(String version, boolean global);
}
