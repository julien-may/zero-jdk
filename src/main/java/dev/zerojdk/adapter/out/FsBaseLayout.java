package dev.zerojdk.adapter.out;

import dev.zerojdk.domain.port.out.BaseLayout;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class FsBaseLayout implements BaseLayout {
    /**
     * Locate the directory that contains a *zjdk* configuration.
     *
     * Starts in the current working directory and walks up the filesystem hierarchy, ignoring the user’s home directory
     * and stops before the filesystem root, returning the first ancestor that contains {@code .zjdk}.
     *
     * @return an {@link Optional} with the path of the directory that holds the
     *         zjdk configuration, or {@link Optional#empty()} if none is found
     */
    @Override
    public Optional<Path> discoverProjectRoot() {
        Path home = Path.of(System.getProperty("user.home")).toAbsolutePath();

        Path path = Path.of(".").toAbsolutePath().normalize();
        while (path != null) {
            if (!path.equals(home)) {
                Path candidate = path.resolve(".zjdk");
                if (Files.exists(candidate)) {
                    return Optional.of(path);
                }
            }

            path = path.getParent();
        }

        return Optional.empty();
    }

    @Override
    public Path baseDirectory(boolean global) {
        if (global) {
            return createBaseDirectory(
                Path.of(System.getProperty("user.home")));
        }

        return discoverProjectRoot()
            .orElseThrow(UnmanagedDirectoryException::new)
            .resolve(".zjdk");
    }

    @Override
    public Path configFile(boolean global) {
        return baseDirectory(global).resolve("config.properties");
    }

    @Override
    public void ensureBaseDirectory(boolean global) {
        if (global) {
            createBaseDirectory(
                Path.of(System.getProperty("user.home")));
        } else {
            createBaseDirectory(Path.of("."));
        }
    }

    @SneakyThrows
    private Path createBaseDirectory(Path root) {
        return Files.createDirectories(
            root.resolve(".zjdk"));
    }
}
