package dev.zerojdk.adapter.out.wrapper;

import dev.zerojdk.domain.model.WrapperConfig;
import dev.zerojdk.domain.port.out.ProjectLayout;
import dev.zerojdk.domain.port.out.wrapper.WrapperConfigRepository;
import dev.zerojdk.domain.service.ConfigurationNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

@RequiredArgsConstructor
public class FsWrapperConfigRepository implements WrapperConfigRepository {
    private static final String FILE_NAME = "zjdk-wrapper.properties";

    private final ProjectLayout projectLayout;

    @Override
    public String propertiesFileName() {
        return FILE_NAME;
    }

    @Override
    public Optional<WrapperConfig> read() {
        return propertiesLocation()
            .filter(Files::exists)
            .map(this::read);
    }

    @SneakyThrows
    @Override
    public void write(WrapperConfig wrapperConfig) {
        Path configPath = propertiesLocation(projectLayout.findProjectRoot(false)
            .orElseThrow(ConfigurationNotFoundException::new));  // TODO: Use different exception

        Files.createDirectories(configPath.getParent());

        Properties properties = new Properties();
        properties.setProperty("url", wrapperConfig.url());

        try (OutputStream out = Files.newOutputStream(configPath)) {
            properties.store(out, null);
        }
    }

    private Optional<Path> propertiesLocation() {
        return projectLayout.findProjectRoot(false)
            .map(this::propertiesLocation);
    }

    private Path propertiesLocation(Path parent) {
        return parent.resolve(".zjdk/wrapper/", FILE_NAME);
    }

    @SneakyThrows
    private WrapperConfig read(Path path) {
        Properties properties = new Properties();

        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
        }

        return new WrapperConfig(
            properties.getProperty("url"));
    }
}
