package dev.zerojdk.adapter.out.index;

import dev.zerojdk.domain.model.IndexEntry;
import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.index.RegistrationRepository;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public class PropertiesRegistrationRepository implements RegistrationRepository {
    private static final File ZJDK_FOLDER = new File(System.getProperty("user.home"), ".zjdk");
    private static final File RELEASES_FOLDER = new File(ZJDK_FOLDER, "releases");

    @SneakyThrows
    @Override
    public void register(JdkVersion jdkVersion, Path releaseRoot, Path javaHome) {
        File info = new File(releaseRoot.toFile(), ".info");

        Properties props = new Properties();
        props.setProperty("home", javaHome.toString());

        try (FileOutputStream fos = new FileOutputStream(info)) {
            props.store(fos, null);
        }
    }

    @SneakyThrows
    @Override
    public Optional<IndexEntry> find(String identifier) {
        File release = new File(RELEASES_FOLDER, identifier);
        File info = new File(release, ".info");

        if (!info.exists()) {
            return Optional.empty();
        }

        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(info)) {
            properties.load(fis);
        }

        return Optional.ofNullable(properties.getProperty("home"))
            .map(home -> new IndexEntry(identifier, release.getAbsolutePath(), home));
    }
}
