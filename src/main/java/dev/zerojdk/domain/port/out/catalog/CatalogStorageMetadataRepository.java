package dev.zerojdk.domain.port.out.catalog;

import dev.zerojdk.domain.model.catalog.storage.CatalogStorage;

import java.util.Optional;

public interface CatalogStorageMetadataRepository {
    Optional<CatalogStorage> getCurrentVersion();
    CatalogStorage setCurrentVersion(String version);
}