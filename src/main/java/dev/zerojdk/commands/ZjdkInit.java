package dev.zerojdk.commands;

import dev.zerojdk.domain.port.out.catalog.CatalogRepository;
import dev.zerojdk.domain.service.ConfigService;
import dev.zerojdk.domain.service.JdkReleaseService;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine;

import java.io.File;

@RequiredArgsConstructor
@CommandLine.Command(header = "Create a manifest in the current or global directory")
public class ZjdkInit implements Runnable {
    private final CatalogRepository catalogRepository;
    private final ConfigService configService;
    private final JdkReleaseService jdkReleaseService;

    @CommandLine.Option(names = {"--version"}, description = "Initialize with this JDK version")
    private String identifier;

    @CommandLine.Option(names = {"--global"}, description = "Initialize globally")
    private boolean global;

    @Override
    public void run() {
        System.out.print("Initializing ZJDK... ");

        try {
            configService.createConfiguration(identifier, global);

            // Sync
            new ZjdkSync(catalogRepository, configService, jdkReleaseService).sync(global);

            System.out.println("done");
        } catch (Exception ex) {
            System.out.println("failed");
            throw ex;
        }
    }
}
