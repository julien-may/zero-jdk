package dev.zerojdk.utils;

public enum OperatingSystem {
    LINUX,
    MACOS,
    WINDOWS,
    AIX;

    public static OperatingSystem detectOperatingSystem() {
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
}
