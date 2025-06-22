package dev.zerojdk.domain.port.out.catalog;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.model.Platform;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CatalogRepository {
    Map<String, List<JdkVersion>> findAll(Platform platform);
    List<JdkVersion> findAllByDistribution(Platform platform, String distribution);

    Map<String, List<JdkVersion>> findLatest(Platform platform);
    List<JdkVersion> findLatestByDistribution(Platform platform, String distribution);

    Optional<JdkVersion> findByIdentifier(Platform platform, String identifier);
}
