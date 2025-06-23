package dev.zerojdk.adapter.out.index;

import dev.zerojdk.domain.model.InstallationRecord;
import dev.zerojdk.domain.port.out.index.RegistrationRepository;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public class FsRegistrationRepository implements RegistrationRepository {
    private static final File ZJDK_FOLDER = new File(System.getProperty("user.home"), ".zjdk");
    private static final File RELEASES_FOLDER = new File(ZJDK_FOLDER, "releases");

    @SneakyThrows
    @Override
    public void register(InstallationRecord installationRecord) {
        File info = installationRecord.installRoot()
            .resolve(".info")
            .toFile();

        Properties props = new Properties();
        props.setProperty("home", installationRecord.javaHome().toAbsolutePath().toString());

        try (FileOutputStream fos = new FileOutputStream(info)) {
            props.store(fos, null);
        }
    }

    @SneakyThrows
    @Override
    public Optional<InstallationRecord> find(String identifier) {
        Path release = RELEASES_FOLDER.toPath()
            .resolve(identifier);

        File info = release
            .resolve(".info")
            .toFile();

        if (!info.exists()) {
            return Optional.empty();
        }

        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(info)) {
            properties.load(fis);
        }

        return Optional.ofNullable(properties.getProperty("home"))
            .map(home -> new InstallationRecord(identifier, release.toAbsolutePath(), Path.of(home)));
    }
}
