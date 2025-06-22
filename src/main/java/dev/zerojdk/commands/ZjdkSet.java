package dev.zerojdk.commands;

import dev.zerojdk.UnsupportedIdentifierException;
import dev.zerojdk.domain.port.out.catalog.CatalogRepository;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static dev.zerojdk.utils.OperatingSystem.detectOperatingSystem;
import static dev.zerojdk.utils.ProcessorArchitecture.detectProcessorArchitecture;

@CommandLine.Command(header = "Update the current or global manifest to a new JDK")
public class ZjdkSet {
    @RequiredArgsConstructor
    @CommandLine.Command(header = "Version to change")
    public static class Version implements Runnable {
        private final CatalogRepository catalogRepository;

        @CommandLine.Option(names = {"--global"}, description = "Set globally")
        private boolean global;

        @CommandLine.Parameters(index = "0", description = "The JDK identifier")
        private String identifier;

        @Override
        public void run() {
            // Verify identifier
            catalogRepository.findByIdentifier(detectOperatingSystem(), detectProcessorArchitecture(), identifier)
                .orElseThrow(() -> new UnsupportedIdentifierException(identifier));

            // Update config file
            updateConfig(identifier, global);

            // sync
            new ZjdkSync(catalogRepository).sync(global);
        }

        private void updateConfig(String identifier, boolean global) {
            Path config = ZjdkSync.findZjdkConfiguration(global
                ? ZjdkSync.SearchMode.USER_HOME
                : ZjdkSync.SearchMode.WORKSPACE);

            updateConfig(identifier, config);
        }

        public static void updateConfig(String identifier, Path config) {
            Properties props = new Properties();
            props.setProperty("version", identifier);

            try (FileOutputStream fileOutputStream = new FileOutputStream(config.toFile())) {
                props.store(fileOutputStream, null);
            } catch (IOException e) {
                throw new RuntimeException("Failed to update config", e);
            }
        }
    }
}
