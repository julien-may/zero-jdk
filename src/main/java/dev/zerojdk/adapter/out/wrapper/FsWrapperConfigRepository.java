package dev.zerojdk.adapter.out.wrapper;

import dev.zerojdk.domain.model.wrapper.WrapperConfig;
import dev.zerojdk.domain.port.out.wrapper.WrapperConfigRepository;
import dev.zerojdk.domain.port.out.wrapper.WrapperLayout;
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
    private final WrapperLayout wrapperLayout;

    @SneakyThrows
    @Override
    public Optional<WrapperConfig> read() {
        Path path = wrapperLayout.configPath();

        if (!Files.exists(path)) {
            return Optional.empty();
        }

        try (InputStream in = Files.newInputStream(path)) {
            Properties properties = new Properties();
            properties.load(in);

            return Optional.of(new WrapperConfig(properties.getProperty("url")));
        }
    }

    @SneakyThrows
    @Override
    public WrapperConfig write(WrapperConfig wrapperConfig) {
        Path configPath = wrapperLayout.configPath();
        Files.createDirectories(configPath.getParent());

        Properties props = new Properties();
        props.setProperty("url", wrapperConfig.url());

        try (OutputStream out = Files.newOutputStream(configPath)) {
            props.store(out, null);
        }

        return wrapperConfig;
    }
}
