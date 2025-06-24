package dev.zerojdk.adapter.in.cli;

import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.port.out.PlatformDetection;
import dev.zerojdk.domain.service.JdkConfigService;
import dev.zerojdk.domain.service.ManifestSyncService;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine;

@CommandLine.Command(header = "Update the current or global manifest to a new JDK")
public class ZjdkSet {
    @RequiredArgsConstructor
    @CommandLine.Command(header = "Version to change")
    public static class Version implements Runnable {
        private final PlatformDetection platformDetection;
        private final JdkConfigService jdkConfigService;
        private final ManifestSyncService manifestSyncService;

        @CommandLine.Option(names = {"--global"}, description = "Set globally")
        private boolean global;

        @CommandLine.Parameters(index = "0", description = "The JDK version")
        private String version;

        @Override
        public void run() {
            Platform platform = platformDetection.detect();

            jdkConfigService.updateConfiguration(platform, version, global);
            manifestSyncService.sync(platform, global);
        }
    }
}
