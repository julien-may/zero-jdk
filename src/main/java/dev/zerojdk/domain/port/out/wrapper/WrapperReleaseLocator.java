package dev.zerojdk.domain.port.out.wrapper;

import dev.zerojdk.domain.model.Platform;

public interface WrapperReleaseLocator {
    String findLatestUrl(Platform platform);
}
