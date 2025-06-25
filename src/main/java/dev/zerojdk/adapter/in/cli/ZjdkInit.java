package dev.zerojdk.adapter.in.cli;

import dev.zerojdk.adapter.in.cli.event.CompositeConsoleEventHandler;
import dev.zerojdk.adapter.in.cli.event.JdkDownloadProgressPrinter;
import dev.zerojdk.adapter.out.config.ConfigFileAlreadyExistsException;
import dev.zerojdk.adapter.out.event.InMemoryDomainEventPublisher;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.port.out.PlatformDetection;
import dev.zerojdk.domain.service.config.JdkConfigService;
import dev.zerojdk.domain.service.sync.ManifestSyncService;
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
        new CompositeConsoleEventHandler(
            new JdkDownloadProgressPrinter()).register(eventPublisher);

        Platform platform = platformDetection.detect();

        try {
            jdkConfigService.createConfiguration(platform, version, global);
        } catch (ConfigFileAlreadyExistsException e) {
            System.err.println("Already initialized. Run 'set version' to modify.");
        }

        manifestSyncService.sync(platform, global);
    }
}
