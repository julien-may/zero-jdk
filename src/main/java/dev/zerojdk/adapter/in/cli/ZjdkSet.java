package dev.zerojdk.adapter.in.cli;

import dev.zerojdk.domain.port.out.catalog.CatalogRepository;
import dev.zerojdk.domain.service.ConfigService;
import dev.zerojdk.domain.service.JdkReleaseService;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine;

@CommandLine.Command(header = "Update the current or global manifest to a new JDK")
public class ZjdkSet {
    @RequiredArgsConstructor
    @CommandLine.Command(header = "Version to change")
    public static class Version implements Runnable {
        private final CatalogRepository catalogRepository;
        private final ConfigService configService;
        private final JdkReleaseService jdkReleaseService;

        @CommandLine.Option(names = {"--global"}, description = "Set globally")
        private boolean global;

        @CommandLine.Parameters(index = "0", description = "The JDK version")
        private String version;

        @Override
        public void run() {
            configService.updateConfiguration(version, global);

            // sync
            new ZjdkSync(catalogRepository, configService, jdkReleaseService).sync(global);
        }
    }
}
