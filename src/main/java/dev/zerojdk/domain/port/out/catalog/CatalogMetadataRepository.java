package dev.zerojdk.domain.port.out.catalog;

import java.util.Optional;

public interface CatalogMetadataRepository {
    Optional<CatalogMetadata> getCurrentVersion();
    CatalogMetadata setCurrentVersion(String version);
}