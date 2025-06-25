package dev.zerojdk.adapter.out.release;

import dev.zerojdk.domain.model.release.InstallationRecord;
import dev.zerojdk.domain.port.out.release.JdkRegistrationRepository;
import dev.zerojdk.domain.port.out.release.JdkReleaseLayout;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

@RequiredArgsConstructor
public class FsJdkRegistrationRepository implements JdkRegistrationRepository {
    private final JdkReleaseLayout jdkReleaseLayout;

    @SneakyThrows
    @Override
    public InstallationRecord register(InstallationRecord installationRecord) {
        File info = installationRecord.installRoot()
            .resolve(".info")
            .toFile();

        Properties props = new Properties();
        props.setProperty("home", installationRecord.javaHome().toAbsolutePath().toString());

        try (FileOutputStream fos = new FileOutputStream(info)) {
            props.store(fos, null);
        }

        return installationRecord;
    }

    @SneakyThrows
    @Override
    public Optional<InstallationRecord> find(String identifier) {
        Path release = jdkReleaseLayout.ensureReleaseDirectory()
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
