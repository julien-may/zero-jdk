package dev.zerojdk.adapter.out;

import dev.zerojdk.domain.model.OperatingSystem;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.model.ProcessorArchitecture;
import dev.zerojdk.domain.port.out.PlatformDetection;

import static dev.zerojdk.domain.model.OperatingSystem.*;
import static dev.zerojdk.domain.model.ProcessorArchitecture.*;

public class SystemPropertyBasedPlatformDetection implements PlatformDetection {
    @Override
    public Platform detect() {
        return new Platform(detectOperatingSystem(), detectArchitecture());
    }

    private OperatingSystem detectOperatingSystem() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("linux")) {
            return LINUX;
        }

        if (os.contains("mac")) {
            return MACOS;
        }

        if (os.contains("win")) {
            return WINDOWS;
        }

        if (os.contains("aix")) {
            return AIX;
        }

        throw new UnsupportedOperationException("Operating System not supported: " + os);
    }

    private ProcessorArchitecture detectArchitecture() {
        String architecture = System.getProperty("os.arch").toLowerCase();

        if (architecture.contains("aarch64")) {
            return AARCH64;
        }

        if (architecture.contains("x86_64") || architecture.contains("amd64")) {
            return X64;
        }

        throw new UnsupportedOperationException("Processor architecture not supported: " + architecture);
    }
}
