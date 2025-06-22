package dev.zerojdk.domain.service;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.catalog.CatalogRepository;
import dev.zerojdk.domain.port.out.config.ConfigRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import dev.zerojdk.domain.model.Platform;

@RequiredArgsConstructor
public class ConfigService {
    private final ConfigRepository configRepository;
    private final CatalogRepository catalogRepository;

    public Optional<String> getActiveVersion(boolean global) {
        return configRepository.readVersion(global);
    }

    public void updateConfiguration(Platform platform, String identifier, boolean global) {
        catalogRepository.findByIdentifier(platform, identifier)
            .orElseThrow(() -> new UnsupportedIdentifierException(identifier));

        configRepository.writeVersion(global, identifier);
    }

    public void createConfiguration(Platform platform, String identifier, boolean global) {
        if (identifier == null) {
            identifier = catalogRepository.findLatestByDistribution(platform, "Temurin").stream()
                .filter(jdkVersion -> jdkVersion.getSupport() == JdkVersion.Support.LTS)
                .map(JdkVersion::getIdentifier)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("There was an issue resolving the default identifier"));
        }

        updateConfiguration(platform, identifier, global);
    }
}
