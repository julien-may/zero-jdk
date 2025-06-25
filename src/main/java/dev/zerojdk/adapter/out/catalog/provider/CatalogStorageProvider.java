package dev.zerojdk.adapter.out.catalog.provider;

import dev.zerojdk.domain.model.catalog.storage.CatalogStorage;

public interface CatalogStorageProvider {
    CatalogStorage provide();
}
