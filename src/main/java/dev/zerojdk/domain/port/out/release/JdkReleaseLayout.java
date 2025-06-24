package dev.zerojdk.domain.port.out.release;

import java.nio.file.Path;

public interface JdkReleaseLayout {
    Path getReleaseDirectory();
}
