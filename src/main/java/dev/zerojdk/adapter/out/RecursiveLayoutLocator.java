package dev.zerojdk.adapter.out;

import dev.zerojdk.domain.port.out.ProjectLayoutPort;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class RecursiveLayoutLocator implements ProjectLayoutPort {
    /**
     * Locate the directory that contains a *zjdk* configuration.
     *
     * if {@code global == true}) checks only the user’s home directory for {@code ~/.zjdk/config.properties}; otherwise
     * starts in the current working directory and walks up the filesystem hierarchy, ignoring the user’s home directory
     * and stops before the filesystem root, returning the first ancestor that contains {@code .zjdk/config.properties}.
     *
     * @param global {@code true} to search exclusively in the user’s home
     *               directory; {@code false} to search upward from the current
     *               directory (excluding the home directory).
     * @return an {@link Optional} with the path of the directory that holds the
     *         zjdk configuration, or {@link Optional#empty()} if none is found
     */
    @Override
    public Optional<Path> findProjectRoot(boolean global) {
        Path home = Path.of(System.getProperty("user.home")).toAbsolutePath();

        if (global) {
            Path candidate = home.resolve(".zjdk").resolve("config.properties");
            return Files.exists(candidate) ? Optional.of(home) : Optional.empty();
        }

        Path path = Path.of(".").toAbsolutePath().normalize();
        while (path != null) {
            if (!path.equals(home)) {
                Path candidate = path.resolve(".zjdk").resolve("config.properties");
                if (Files.exists(candidate)) {
                    return Optional.of(path);
                }
            }

            path = path.getParent();
        }

        return Optional.empty();
    }
}
