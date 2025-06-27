package dev.zerojdk.domain.port.out;

import java.nio.file.Path;
import java.util.Optional;

public interface BaseLayout {
    Optional<Path> discoverProjectRoot();

    Path configFile(boolean global);

    Path ensureBaseDirectory(boolean global);
}
