package dev.zerojdk.domain.port.out.wrapper;

import dev.zerojdk.domain.model.WrapperConfig;

import java.util.Optional;

public interface WrapperConfigStorePort {
    String propertiesFileName();
    Optional<WrapperConfig> read();
    void write(WrapperConfig url);
}
