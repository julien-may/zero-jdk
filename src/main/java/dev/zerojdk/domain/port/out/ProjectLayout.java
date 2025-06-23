package dev.zerojdk.domain.port.out;

import java.nio.file.Path;
import java.util.Optional;

public interface ProjectLayout {
    Optional<Path> findProjectRoot(boolean global);
}
