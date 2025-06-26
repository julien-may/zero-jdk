package dev.zerojdk.adapter.in.cli;

import dev.zerojdk.adapter.in.cli.event.CompositeConsoleEventHandler;
import dev.zerojdk.adapter.in.cli.event.JdkDownloadProgressPrinter;
import dev.zerojdk.adapter.out.event.InMemoryDomainEventPublisher;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.port.out.PlatformDetection;
import dev.zerojdk.domain.service.sync.ManifestSyncService;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine;

@RequiredArgsConstructor
@CommandLine.Command(header = "Ensure that the JDK declared in the manifest is ready")
public class ZjdkSync implements Runnable {
    private final PlatformDetection platformDetection;
    private final ManifestSyncService manifestSyncService;
    private final InMemoryDomainEventPublisher eventPublisher;

    @CommandLine.Option(names = {"--global"}, description = "Sync globally")
    private boolean global;

    @Override
    public void run() {
        new CompositeConsoleEventHandler(
            new JdkDownloadProgressPrinter()).register(eventPublisher);

        Platform platform = platformDetection.detect();
        manifestSyncService.sync(platform, global);
    }
}
