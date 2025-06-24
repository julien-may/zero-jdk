package dev.zerojdk.domain.port.out;

import dev.zerojdk.domain.model.Platform;

public interface PlatformDetection {
    Platform detect();
}
