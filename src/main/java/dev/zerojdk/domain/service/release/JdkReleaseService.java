package dev.zerojdk.domain.service.release;

import dev.zerojdk.domain.model.JdkRelease;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.port.out.download.DownloadService;
import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.event.DomainEventPublisher;
import dev.zerojdk.domain.port.out.index.RegistrationRepository;
import dev.zerojdk.domain.port.out.release.JdkInstaller;
import dev.zerojdk.domain.port.out.release.JdkReleaseLayout;
import dev.zerojdk.domain.port.out.unarchiving.UnarchiverFactory;
import dev.zerojdk.domain.service.CatalogService;
import dev.zerojdk.domain.service.release.events.JdkDownloadCompleted;
import dev.zerojdk.domain.service.release.events.JdkDownloadProgress;
import dev.zerojdk.domain.service.release.events.JdkDownloadStarted;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

@RequiredArgsConstructor
public class JdkReleaseService {
    private final DomainEventPublisher publisher;
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

        publisher.publish(new JdkDownloadStarted(version));
        File downloadedFile = downloadService.download(version.getIndirectDownloadUri(),
            (bytesRead, totalBytes) ->
                publisher.publish(new JdkDownloadProgress(version, bytesRead, totalBytes)));
        publisher.publish(new JdkDownloadCompleted(version));

        Path extracted = unarchiverFactory.create(downloadedFile)
            .extract(jdkReleaseLayout.ensureReleaseDirectory()
                .resolve(version.getIdentifier()));

        jdkInstaller.install(version, extracted);
    }

    public Optional<JdkRelease> findJdkRelease(Platform platform, String identifier) {
        return repository.find(identifier)
            .flatMap(installationRecord ->
                catalogService.findByIdentifier(platform, installationRecord.identifier())
                    .map(jdkVersion -> new JdkRelease(jdkVersion, installationRecord.installRoot(), installationRecord.javaHome()))
            );
    }
}
