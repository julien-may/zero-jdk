package dev.zerojdk.domain.service.catalog;

import dev.zerojdk.domain.model.catalog.Catalog;
import dev.zerojdk.domain.port.out.catalog.CatalogDownloadService;
import dev.zerojdk.domain.model.catalog.storage.CatalogStorage;
import dev.zerojdk.domain.port.out.catalog.CatalogStorageMetadataRepository;
import lombok.RequiredArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RequiredArgsConstructor
public class CatalogStorageService {
    private final CatalogDownloadService downloadService;
    private final CatalogStorageMetadataRepository metadataRepository;

    public CatalogStorage updateCatalog() {
        Catalog catalog = downloadService.downloadLatest();
        return storeCatalog(catalog);
    }

    public CatalogStorage updateCatalogIfNewer() {
        return metadataRepository.getCurrentVersion()
            .map(CatalogStorage::version)
            .map(this::updateCatalogIfNewer)
            .orElseGet(this::updateCatalog);
    }

    public CatalogStorage bootstrapIfMissing() {
        return metadataRepository.getCurrentVersion()
            .filter(meta -> Files.exists(meta.location()))
            .orElseGet(this::updateCatalog);
    }

    private CatalogStorage updateCatalogIfNewer(String currentVersion) {
        Catalog catalog = downloadService.downloadLatestIfNewer(currentVersion);
        return storeCatalog(catalog);
    }

    private CatalogStorage storeCatalog(Catalog catalog) {
        CatalogStorage metadata = metadataRepository.setCurrentVersion(catalog.version());
        move(catalog.location(), metadata.location());
        return metadata;
    }

    private void move(Path source, Path target) {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Failed to move catalog file", e);
        }
    }
}
