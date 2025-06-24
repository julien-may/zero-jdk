package dev.zerojdk.adapter.out.catalog.provider;

import dev.zerojdk.domain.model.CatalogStorage;
import dev.zerojdk.domain.service.CatalogStorageService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class JsonCatalogStorageProvider implements CatalogStorageProvider {
    private final CatalogStorageService catalogStorageService;

    @SneakyThrows
    @Override
    public CatalogStorage provide() {
        return catalogStorageService.bootstrapIfMissing();
    }
}
