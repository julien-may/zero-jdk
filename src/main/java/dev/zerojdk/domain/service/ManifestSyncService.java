package dev.zerojdk.domain.service;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.catalog.CatalogRepository;
import lombok.RequiredArgsConstructor;

import static dev.zerojdk.utils.OperatingSystem.detectOperatingSystem;
import static dev.zerojdk.utils.ProcessorArchitecture.detectProcessorArchitecture;

@RequiredArgsConstructor
public class ManifestSyncService {
    private final CatalogRepository catalogRepository;
    private final ConfigService configService;
    private final JdkReleaseService jdkReleaseService;

    public void sync(boolean global) {
        String identifier = configService.getActiveVersion(global)
            .orElseThrow(ConfigurationNotFoundException::new);

        JdkVersion configuredJdkVersion = catalogRepository
            .findByIdentifier(detectOperatingSystem(), detectProcessorArchitecture(), identifier)
            .orElseThrow(() -> new UnsupportedIdentifierException(identifier));

        jdkReleaseService.ensureRelease(configuredJdkVersion);
    }
}
