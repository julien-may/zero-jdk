package dev.zerojdk.domain.port.out.release;

import dev.zerojdk.domain.model.JdkVersion;

import java.nio.file.Path;

public interface JdkReleaseLayout {
    Path tempDirectory(JdkVersion version);
    Path ensureReleaseDirectory();
}
