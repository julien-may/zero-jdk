package dev.zerojdk.adapter.out.config;

import dev.zerojdk.domain.port.out.BaseLayout;
import dev.zerojdk.domain.port.out.config.JdkConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Properties;

@RequiredArgsConstructor
public class FsJdkConfigRepository implements JdkConfigRepository {
    private final BaseLayout baseLayout;

    @Override
    public String readVersion(boolean global) {
        return readVersionFromFile(baseLayout.configFile(global));
    }

    @Override
    public void writeVersion(boolean global, String version) {
        writeVersion(baseLayout.configFile(global), version);
    }

    @SneakyThrows
    private String readVersionFromFile(Path configFile) {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(configFile.toFile())) {
            props.load(fis);
            return props.getProperty("version");
        }
    }

    @SneakyThrows
    private void writeVersion(Path configFile, String version) {
        Properties props = new Properties();
        props.setProperty("version", version);

        try (FileOutputStream fos = new FileOutputStream(configFile.toFile())) {
            props.store(fos, null);
        }
    }
}
