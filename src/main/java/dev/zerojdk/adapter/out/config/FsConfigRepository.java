package dev.zerojdk.adapter.out.config;

import dev.zerojdk.domain.port.out.ProjectLayoutPort;
import dev.zerojdk.domain.port.out.config.ConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

@RequiredArgsConstructor
public class FsConfigRepository implements ConfigRepository {
    private final ProjectLayoutPort projectLayoutPort;

    @Override
    public Optional<String> readVersion(boolean global) {
        return projectLayoutPort.findProjectRoot(global)
            .map(path -> path.resolve(".zjdk", "config.properties"))
            .flatMap(this::readVersionFromFile);
    }

    @Override
    public void writeVersion(boolean global, String version) {
        Path target = global
            ? Path.of(System.getProperty("user.home"), ".zjdk", "config.properties")
            : Path.of(".zjdk", "config.properties");

        writeVersion(target, version);
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
