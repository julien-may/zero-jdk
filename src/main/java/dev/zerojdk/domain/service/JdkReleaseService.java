package dev.zerojdk.domain.service;

import dev.zerojdk.domain.port.out.download.DownloadService;
import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.index.RegistrationRepository;
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
    private final RegistrationRepository repository;

    public void ensureRelease(JdkVersion version) {
        Optional<Path> jdkRelease = findJdkRelease(version.getIdentifier());

        if (jdkRelease.isEmpty()) {
            Path path = downloadAndExtract(version);

            findJavaHome(path)
                .ifPresent(javaHome -> repository.register(version, path, javaHome));
        }
    }

    // TODO: returning the home only is a bit stupid
    public Optional<Path> findJdkRelease(String identifier) {
        return repository.find(identifier)
            .map(indexEntry -> Path.of(indexEntry.javaHome()));
    }

    @SneakyThrows
    private Path downloadAndExtract(JdkVersion version) {
        Path targetParent = Path.of(System.getProperty("user.home"), ".zjdk");
        Path releases = targetParent.resolve("releases");

        Files.createDirectories(releases);

        File downloadedFile = downloadService.download(version.getIndirectDownloadUri());

        return unarchiverFactory.create(downloadedFile)
            .extract(releases.resolve(version.getIdentifier()));
    }

    @SneakyThrows
    private Optional<Path> findJavaHome(Path root) {
        try (Stream<Path> path = Files.walk(root)) {
            return path.filter(p -> p.getFileName().toString().equals("java"))
                .filter(Files::isExecutable)
                .findFirst()
                .map(Path::getParent)
                .map(Path::getParent);
        }
    }
}
