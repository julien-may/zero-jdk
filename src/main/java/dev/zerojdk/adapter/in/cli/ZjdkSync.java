package dev.zerojdk.adapter.in.cli;

import dev.zerojdk.ConfigurationNotFoundException;
import dev.zerojdk.UnsupportedIdentifierException;
import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.catalog.CatalogRepository;
import dev.zerojdk.domain.service.ConfigService;
import dev.zerojdk.domain.service.JdkReleaseService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import picocli.CommandLine;

import static dev.zerojdk.utils.OperatingSystem.*;
import static dev.zerojdk.utils.ProcessorArchitecture.*;

@RequiredArgsConstructor
@CommandLine.Command(header = "Ensure that the JDK declared in the manifest is ready")
public class ZjdkSync implements Runnable {
    private final CatalogRepository catalogRepository;
    private final ConfigService configService;
    private final JdkReleaseService jdkReleaseService;

    @CommandLine.Option(names = {"--global"}, description = "Sync globally")
    private boolean global;

    @SneakyThrows
    @Override
    public void run() {
        // TODO: processing feedback
        //      System.out.printf("Downloading %s...", version.getIdentifier());

        sync(global);
    }

    public void sync(boolean global) {
        String identifier = configService.getActiveVersion(global)
            .orElseThrow(ConfigurationNotFoundException::new);

        JdkVersion configuredJdkVersion = findConfiguredJdkVersion(identifier);

        jdkReleaseService.ensureRelease(configuredJdkVersion);
    }

    private JdkVersion findConfiguredJdkVersion(String identifier) {
        return catalogRepository
            .findByIdentifier(detectOperatingSystem(), detectProcessorArchitecture(), identifier)
            .orElseThrow(() -> new UnsupportedIdentifierException(identifier));
    }
}
