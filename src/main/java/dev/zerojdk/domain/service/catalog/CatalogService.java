package dev.zerojdk.domain.service.catalog;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.port.out.catalog.CatalogRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class CatalogService {
    private final CatalogRepository catalogRepository;

    public List<JdkVersion> findAllByDistribution(Platform platform, String distribution) {
        return catalogRepository.findAllByDistribution(platform, distribution);
    }

    public Map<String, List<JdkVersion>> findLatest(Platform platform) {
        return catalogRepository.findLatest(platform);
    }

    public List<JdkVersion> findLatestByDistribution(Platform platform, String distribution) {
        return catalogRepository.findLatestByDistribution(platform, distribution);
    }

    public Optional<JdkVersion> findByIdentifier(Platform platform, String identifier) {
        return catalogRepository.findByIdentifier(platform, identifier);
    }
}
