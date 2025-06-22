package dev.zerojdk.commands;

import dev.zerojdk.domain.port.out.catalog.CatalogRepository;
import dev.zerojdk.domain.service.ConfigService;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine;

import java.io.File;

@RequiredArgsConstructor
@CommandLine.Command(header = "Create a manifest in the current or global directory")
public class ZjdkInit implements Runnable {
    private final CatalogRepository catalogRepository;
    private final ConfigService configService;

    @CommandLine.Option(names = {"--version"}, description = "Initialize with this JDK version", defaultValue = "foo-bar")
    private String identifier;

    @CommandLine.Option(names = {"--global"}, description = "Initialize globally")
    private boolean global;

    private static final File ZJDK_FOLDER = new File(".zjdk");

    @Override
    public void run() {
        System.out.print("Initializing ZJDK... ");

        try {
            configService.createConfiguration(identifier, global);

            // Sync
            new ZjdkSync(catalogRepository, configService).sync(global);

            System.out.println("done");
        } catch (Exception ex) {
            System.out.println("failed");
            throw ex;
        }
    }
}
