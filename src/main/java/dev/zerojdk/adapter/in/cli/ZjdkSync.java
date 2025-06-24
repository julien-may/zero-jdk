package dev.zerojdk.adapter.in.cli;

import dev.zerojdk.adapter.out.event.InMemoryDomainEventPublisher;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.port.out.PlatformDetection;
import dev.zerojdk.domain.service.ManifestSyncService;
import dev.zerojdk.domain.service.release.events.JdkDownloadCompleted;
import dev.zerojdk.domain.service.release.events.JdkDownloadProgress;
import dev.zerojdk.domain.service.release.events.JdkDownloadStarted;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import picocli.CommandLine;

@RequiredArgsConstructor
@CommandLine.Command(header = "Ensure that the JDK declared in the manifest is ready")
public class ZjdkSync implements Runnable {
    private final PlatformDetection platformDetection;
    private final ManifestSyncService manifestSyncService;
    private final InMemoryDomainEventPublisher eventPublisher;

    @CommandLine.Option(names = {"--global"}, description = "Sync globally")
    private boolean global;

    @SneakyThrows
    @Override
    public void run() {
        eventPublisher.register(JdkDownloadStarted.class, e ->
            System.out.printf("Downloading: %s...", e.version().getIdentifier()));
        eventPublisher.register(JdkDownloadProgress.class, e ->
            System.out.printf("\rDownloading: %s... %d%%", e.version().getIdentifier(), e.bytesRead() * 100 / e.totalBytes()));
        eventPublisher.register(JdkDownloadCompleted.class, e ->
            System.out.println("\ndone"));

        Platform platform = platformDetection.detect();
        manifestSyncService.sync(platform, global);
    }
}
