package dev.zerojdk.adapter.in.cli;

import dev.zerojdk.adapter.out.event.InMemoryDomainEventPublisher;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.port.out.PlatformDetection;
import dev.zerojdk.domain.service.JdkConfigService;
import dev.zerojdk.domain.service.ManifestSyncService;
import dev.zerojdk.domain.service.release.events.JdkDownloadCompleted;
import dev.zerojdk.domain.service.release.events.JdkDownloadProgress;
import dev.zerojdk.domain.service.release.events.JdkDownloadStarted;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine;

@RequiredArgsConstructor
@CommandLine.Command(header = "Create a manifest in the current or global directory")
public class ZjdkInit implements Runnable {
    private final PlatformDetection platformDetection;
    private final JdkConfigService jdkConfigService;
    private final ManifestSyncService manifestSyncService;
    private final InMemoryDomainEventPublisher eventPublisher;

    @CommandLine.Option(names = {"--version"}, description = "Initialize with this JDK version")
    private String version;

    @CommandLine.Option(names = {"--global"}, description = "Initialize globally")
    private boolean global;

    @Override
    public void run() {
        eventPublisher.register(JdkDownloadStarted.class, e ->
            System.out.printf("Downloading: %s...", e.version().getIdentifier()));
        eventPublisher.register(JdkDownloadProgress.class, e ->
            System.out.printf("\rDownloading: %s... %d%%", e.version().getIdentifier(), e.bytesRead() * 100 / e.totalBytes()));
        eventPublisher.register(JdkDownloadCompleted.class, e ->
            System.out.println("\ndone"));

//        System.out.print("Initializing... ");

        Platform platform = platformDetection.detect();

        jdkConfigService.createConfiguration(platform, version, global);
        manifestSyncService.sync(platform, global);
    }
}
