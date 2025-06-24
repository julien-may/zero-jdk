package dev.zerojdk.domain.port.out.catalog;

import java.nio.file.Path;

public interface CatalogStorageLayout {
    Path metadataFile();
    Path catalogFile();
}
