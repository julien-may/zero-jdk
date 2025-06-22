package dev.zerojdk.commands;

import dev.zerojdk.ConfigurationNotFoundException;
import dev.zerojdk.UnsupportedIdentifierException;
import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.catalog.CatalogRepository;
import dev.zerojdk.domain.service.ConfigService;
import dev.zerojdk.domain.service.JdkReleaseService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.*;
import java.util.*;

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

    private record JdkPaths(JdkVersion version, Path extractedRoot, Path javaHome) {}

    @SneakyThrows
    @Override
    public void run() {
        // TODO: processing feedback
        //      System.out.printf("Downloading %s...", version.getIdentifier());

        sync(global);
    }

    public void sync(boolean global) {
        String identifier = configService.getConfiguredIdentifier(global)
            .orElseThrow(ConfigurationNotFoundException::new);

        JdkVersion configuredJdkVersion = findConfiguredJdkVersion(identifier);

        jdkReleaseService.ensureRelease(configuredJdkVersion)
            .flatMap(root -> jdkReleaseService.findJavaHome(root)
                .map(javaHome -> new JdkPaths(configuredJdkVersion, root, javaHome)))
        .ifPresent(paths -> registerJdk(
            paths.version(),
            paths.extractedRoot(),
            paths.javaHome()));
    }

    public record IndexEntry(String identifier, String release, String javaHome) { }


    @SneakyThrows
    private static void registerJdk(JdkVersion jdkVersion, Path release, Path javaHome) {
        File info = new File(release.toFile(), ".info");

        Properties props = new Properties();
        props.setProperty("home", javaHome.toString());

        try (FileOutputStream fileOutputStream = new FileOutputStream(info)) {
            props.store(fileOutputStream, null);
        }
    }

    private JdkVersion findConfiguredJdkVersion(String identifier) {
        return catalogRepository
            .findByIdentifier(detectOperatingSystem(), detectProcessorArchitecture(), identifier)
            .orElseThrow(() -> new UnsupportedIdentifierException(identifier));
    }
}
