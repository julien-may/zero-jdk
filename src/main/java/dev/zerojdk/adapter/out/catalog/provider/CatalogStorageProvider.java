package dev.zerojdk.adapter.out.catalog.provider;

import dev.zerojdk.domain.port.out.catalog.CatalogMetadata;

public interface CatalogStorageProvider {
    CatalogMetadata provide();
}
