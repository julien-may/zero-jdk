package dev.zerojdk.domain.service;

import dev.zerojdk.domain.model.JdkRelease;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.port.out.download.DownloadService;
import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.index.RegistrationRepository;
import dev.zerojdk.domain.port.out.release.JdkInstaller;
import dev.zerojdk.domain.port.out.release.JdkReleaseLayout;
import dev.zerojdk.infrastructure.unarchiver.UnarchiverFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@RequiredArgsConstructor
public class JdkReleaseService {
    private final JdkReleaseLayout jdkReleaseLayout;
    private final DownloadService downloadService;
    private final UnarchiverFactory unarchiverFactory;
    private final CatalogService catalogService;
    private final RegistrationRepository repository;
    private final JdkInstaller jdkInstaller;

    @SneakyThrows
    public void installIfAbsent(JdkVersion version) {
        if (findJdkRelease(version.getPlatform(), version.getIdentifier()).isPresent()) {
            return;
        }

        File downloadedFile = downloadService.download(version.getIndirectDownloadUri());

        Path extracted = unarchiverFactory.create(downloadedFile)
            .extract(resolveReleaseDirectoryFor(version.getIdentifier()));

        jdkInstaller.install(version, extracted);
    }

    @SneakyThrows
    private Path resolveReleaseDirectoryFor(String version) {
        // TODO: creating directories here might not be very clean
        return Files.createDirectories(jdkReleaseLayout.getReleaseDirectory())
            .resolve(version);
    }

    public Optional<JdkRelease> findJdkRelease(Platform platform, String identifier) {
        return repository.find(identifier)
            .flatMap(installationRecord ->
                catalogService.findByIdentifier(platform, installationRecord.identifier())
                    .map(jdkVersion -> new JdkRelease(jdkVersion, installationRecord.installRoot(), installationRecord.javaHome()))
            );
    }
}
