package dev.zerojdk.domain.service;

import dev.zerojdk.domain.port.out.download.DownloadService;
import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.infrastructure.unarchiver.UnarchiverFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class JdkReleaseService {
    private final DownloadService downloadService;
    private final UnarchiverFactory unarchiverFactory;

    public Optional<Path> ensureRelease(JdkVersion version) {
        return findJdkRelease(version)
            .or(() -> downloadAndExtract(version));
    }

    private Optional<Path> findJdkRelease(JdkVersion jdkVersion) {
        File zjdkHome = new File(System.getProperty("user.home"), ".zjdk");
        File releases = new File(zjdkHome, "releases");
        File jdkRelease = new File(releases, jdkVersion.getIdentifier());

        if (jdkRelease.exists()) {
            return Optional.of(jdkRelease.toPath());
        }

        return Optional.empty();
    }

    @SneakyThrows
    private Optional<Path> downloadAndExtract(JdkVersion version) {
        Path targetParent = Path.of(System.getProperty("user.home"), ".zjdk");
        Path releases = targetParent.resolve("releases");

        Files.createDirectories(releases);

        File downloadedFile = downloadService.download(version.getIndirectDownloadUri());
        Path extracted = unarchiverFactory.create(downloadedFile)
            .extract(releases.resolve(version.getIdentifier()));

        return Optional.of(extracted);
    }

    @SneakyThrows
    public Optional<Path> findJavaHome(Path root) {
        try (Stream<Path> path = Files.walk(root)) {
            return path.filter(p -> p.getFileName().toString().equals("java"))
                .filter(Files::isExecutable)
                .findFirst()
                .map(Path::getParent)
                .map(Path::getParent);
        }
    }
}
