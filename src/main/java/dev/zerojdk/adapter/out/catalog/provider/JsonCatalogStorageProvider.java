package dev.zerojdk.adapter.out.catalog.provider;

import dev.zerojdk.domain.model.catalog.storage.CatalogStorage;
import dev.zerojdk.domain.service.catalog.CatalogStorageService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JsonCatalogStorageProvider implements CatalogStorageProvider {
    private final CatalogStorageService catalogStorageService;

    @Override
    public CatalogStorage provide() {
        return catalogStorageService.findCatalogStorage()
            .orElseGet(catalogStorageService::updateCatalog);
    }
}
