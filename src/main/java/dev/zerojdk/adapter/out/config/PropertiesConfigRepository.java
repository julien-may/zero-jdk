package dev.zerojdk.adapter.out.config;

import dev.zerojdk.domain.port.out.config.ConfigRepository;
import lombok.SneakyThrows;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public class PropertiesConfigRepository implements ConfigRepository {
    @Override
    public Optional<String> readVersion(boolean global) {
        return findConfig(global)
            .flatMap(this::readVersionFromFile);
    }

    @Override
    public void writeVersion(boolean global, String version) {
        Path target = global
            ? Path.of(System.getProperty("user.home"), ".zjdk", "config.properties")
            : Path.of(".zjdk", "config.properties");

        writeVersion(target, version);
    }

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
    private Optional<Path> findConfig(boolean global) {
        Path home = Path.of(System.getProperty("user.home")).toAbsolutePath();

        if (global) {
            Path candidate = home.resolve(".zjdk").resolve("config.properties");
            return Files.exists(candidate) ? Optional.of(candidate) : Optional.empty();
        }

        Path path = Path.of(".").toAbsolutePath().normalize();
        while (path != null) {
            if (!path.equals(home)) {
                Path candidate = path.resolve(".zjdk").resolve("config.properties");
                if (Files.exists(candidate)) {
                    return Optional.of(candidate);
                }
            }

            Path parent = path.getParent();
            if (parent == null) {
                break;
            }
            path = parent;
        }

        return Optional.empty();
    }

    @SneakyThrows
    private Optional<String> readVersionFromFile(Path configFile) {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(configFile.toFile())) {
            props.load(fis);
            return Optional.ofNullable(props.getProperty("version"));
        }
    }

    @SneakyThrows
    private void writeVersion(Path configFile, String version) {
        Properties props = new Properties();
        props.setProperty("version", version);

        Files.createDirectories(configFile.getParent());
        try (FileOutputStream fos = new FileOutputStream(configFile.toFile())) {
            props.store(fos, null);
        }
    }
}
