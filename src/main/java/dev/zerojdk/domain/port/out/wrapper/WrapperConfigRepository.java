package dev.zerojdk.domain.port.out.wrapper;

import dev.zerojdk.domain.model.WrapperConfig;

import java.util.Optional;

public interface WrapperConfigRepository {
    Optional<WrapperConfig> read();
    WrapperConfig write(WrapperConfig url);
}
