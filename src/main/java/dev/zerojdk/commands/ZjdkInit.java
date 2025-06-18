package dev.zerojdk.commands;

import dev.zerojdk.UnsupportedIdentifierException;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.zerojdk.utils.OperatingSystem.detectOperatingSystem;
import static dev.zerojdk.utils.ProcessorArchitecture.detectProcessorArchitecture;

@CommandLine.Command(header = "Create a manifest in the current or global directory")
public class ZjdkInit implements Runnable {
    @CommandLine.Option(names = {"--version"}, description = "Initialize with this JDK version", defaultValue = "foo-bar")
    private String identifier;

    @CommandLine.Option(names = {"--global"}, description = "Initialize globally")
    private boolean global;

    private static final File ZJDK_FOLDER = new File(".zjdk");
    private static final File ZJDK_PROPERTIES = new File("config.properties");

    @Override
    public void run() {
        System.out.print("Initializing ZJDK... ");

        try {
            File parentPath = global
                ? new File(System.getProperty("user.home"))
                : new File(".");

            Path path = new File(parentPath, ZJDK_FOLDER.getPath()).toPath();

            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ZjdkSync.findByIdentifier(detectOperatingSystem(), detectProcessorArchitecture(), identifier)
                .orElseThrow(() -> new UnsupportedIdentifierException(identifier));

            File file = new File(path.toFile(), ZJDK_PROPERTIES.getPath());

            // TODO: if config.properties already exists, check if there is also a content. If so, don't do anything

            ZjdkSet.Version.updateConfig(identifier, file.toPath());

            // Sync
            ZjdkSync.sync(global);

            System.out.println("done");
        } catch (Exception ex) {
            System.out.println("failed");
            throw ex;
        }
    }
}
