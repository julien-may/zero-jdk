package dev.zerojdk.domain.model;

import dev.zerojdk.utils.OperatingSystem;
import dev.zerojdk.utils.ProcessorArchitecture;

public record Platform(OperatingSystem os, ProcessorArchitecture arch) {
    public static Platform detect() {
        return new Platform(OperatingSystem.detect(), ProcessorArchitecture.detect());
    }
}