package dev.zerojdk.adapter.out.catalog.provider;

import dev.zerojdk.domain.port.out.catalog.CatalogMetadata;
import dev.zerojdk.domain.port.out.catalog.CatalogMetadataRepository;
import dev.zerojdk.domain.model.Catalog;
import dev.zerojdk.domain.service.CatalogDownloadService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@RequiredArgsConstructor
public class JsonCatalogStorageProvider implements CatalogStorageProvider {
    private final CatalogDownloadService catalogDownloadService;
    private final CatalogMetadataRepository catalogMetadataRepository;

    @SneakyThrows
    @Override
    public CatalogMetadata provide() {
        Optional<CatalogMetadata> currentVersion = catalogMetadataRepository.getCurrentVersion();

        if (currentVersion.isEmpty() || !currentVersion.get().location().toFile().exists()) {
            Catalog catalog = catalogDownloadService.downloadLatestIfNewer(null);
            CatalogMetadata catalogMetadata = catalogMetadataRepository.setCurrentVersion(catalog.version());

            Files.move(catalog.location(), catalogMetadata.location(), StandardCopyOption.REPLACE_EXISTING);

            return catalogMetadata;
        }

        return currentVersion.get();
    }
}
