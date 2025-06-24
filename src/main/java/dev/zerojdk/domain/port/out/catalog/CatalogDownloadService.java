package dev.zerojdk.domain.port.out.catalog;

import dev.zerojdk.domain.model.Catalog;
import dev.zerojdk.domain.service.CatalogUnchangedException;

public interface CatalogDownloadService {
    Catalog downloadLatest() throws CatalogUnchangedException;

    /**
     * Downloads and extracts the latest catalog if the currentVersion is older.
     * Throws CatalogUnchangedException if the current version is already the latest.
     *
     * @param currentVersion the currently installed catalog version
     * @return path to the downloaded catalog file (extracted)
     * @throws CatalogUnchangedException if the catalog is already up-to-date
     */
    Catalog downloadLatestIfNewer(String currentVersion) throws CatalogUnchangedException;
}