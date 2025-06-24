package dev.zerojdk.adapter.in.cli;

import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.port.out.PlatformDetection;
import dev.zerojdk.domain.service.JdkConfigService;
import dev.zerojdk.domain.service.ManifestSyncService;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine;

@RequiredArgsConstructor
@CommandLine.Command(header = "Create a manifest in the current or global directory")
public class ZjdkInit implements Runnable {
    private final PlatformDetection platformDetection;
    private final JdkConfigService jdkConfigService;
    private final ManifestSyncService manifestSyncService;

    @CommandLine.Option(names = {"--version"}, description = "Initialize with this JDK version")
    private String version;

    @CommandLine.Option(names = {"--global"}, description = "Initialize globally")
    private boolean global;

    @Override
    public void run() {
        System.out.print("Initializing... ");

        Platform platform = platformDetection.detect();

        try {
            jdkConfigService.createConfiguration(platform, version, global);
            manifestSyncService.sync(platform, global);

            System.out.println("done");
        } catch (Exception ex) {
            System.out.println("failed");
            throw ex;
        }
    }
}
