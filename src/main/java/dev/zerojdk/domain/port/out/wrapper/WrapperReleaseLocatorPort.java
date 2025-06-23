package dev.zerojdk.domain.port.out.wrapper;

import dev.zerojdk.domain.model.Platform;

public interface WrapperReleaseLocatorPort {
    String findLatestUrl(Platform platform);
}
