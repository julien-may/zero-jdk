package dev.zerojdk.domain.service;

import dev.zerojdk.domain.model.JdkVersion;
import lombok.RequiredArgsConstructor;

import dev.zerojdk.domain.model.Platform;

@RequiredArgsConstructor
public class ManifestSyncService {
    private final CatalogService catalogService;
    private final JdkConfigService jdkConfigService;
    private final JdkReleaseService jdkReleaseService;

    public void sync(Platform platform, boolean global) {
        String identifier = jdkConfigService.getActiveVersion(global);

        JdkVersion configuredJdkVersion = catalogService
            .findByIdentifier(platform, identifier)
            .orElseThrow(() -> new UnsupportedIdentifierException(identifier));

        jdkReleaseService.installIfAbsent(configuredJdkVersion);
    }
}
