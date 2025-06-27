package dev.zerojdk.domain.service.config;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.config.JdkConfigRepository;
import dev.zerojdk.domain.service.catalog.CatalogService;
import lombok.RequiredArgsConstructor;

import dev.zerojdk.domain.model.Platform;

import java.util.Optional;

@RequiredArgsConstructor
public class JdkConfigService {
    private final JdkConfigRepository jdkConfigRepository;
    private final CatalogService catalogService;

    public String getActiveVersion(boolean global) {
        return jdkConfigRepository.readVersion(global);
    }

    public void updateConfiguration(Platform platform, String version, boolean global) {
        catalogService.findByIdentifier(platform, version)
            .orElseThrow(() -> new UnsupportedIdentifierException(version));

        jdkConfigRepository.update(version, global);
    }

    public void createConfiguration(Platform platform, String version, boolean global) {
        String identifier = Optional.ofNullable(version)
            .orElseGet(() -> catalogService.findLatestByDistribution(platform, "Temurin").stream()
                .filter(jdkVersion -> jdkVersion.getSupport() == JdkVersion.Support.LTS)
                .map(JdkVersion::getIdentifier)
                .findFirst()
                // TODO: proper exception
                .orElseThrow(() -> new RuntimeException("There was an issue resolving the default version")));

        catalogService.findByIdentifier(platform, identifier)
            .orElseThrow(() -> new UnsupportedIdentifierException(identifier));

        jdkConfigRepository.create(identifier, global);
    }
}
