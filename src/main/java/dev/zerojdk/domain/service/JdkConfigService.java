package dev.zerojdk.domain.service;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.config.JdkConfigRepository;
import lombok.RequiredArgsConstructor;

import dev.zerojdk.domain.model.Platform;

@RequiredArgsConstructor
public class JdkConfigService {
    private final JdkConfigRepository jdkConfigRepository;
    private final CatalogService catalogService;

    public String getActiveVersion(boolean global) {
        return jdkConfigRepository.readVersion(global);
    }

    public void updateConfiguration(Platform platform, String identifier, boolean global) {
        catalogService.findByIdentifier(platform, identifier)
            .orElseThrow(() -> new UnsupportedIdentifierException(identifier));

        jdkConfigRepository.updateVersion(global, identifier);
    }

    public void createConfiguration(Platform platform, String identifier, boolean global) {
        if (identifier == null) {
            identifier = catalogService.findLatestByDistribution(platform, "Temurin").stream()
                .filter(jdkVersion -> jdkVersion.getSupport() == JdkVersion.Support.LTS)
                .map(JdkVersion::getIdentifier)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("There was an issue resolving the default identifier"));
        }

        updateConfiguration(platform, identifier, global);
    }
}
