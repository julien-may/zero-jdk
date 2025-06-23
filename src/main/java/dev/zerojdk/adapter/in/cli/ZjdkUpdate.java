package dev.zerojdk.adapter.in.cli;

import dev.zerojdk.domain.port.out.catalog.CatalogMetadata;
import dev.zerojdk.domain.port.out.catalog.CatalogMetadataRepository;
import dev.zerojdk.domain.model.Catalog;
import dev.zerojdk.domain.service.CatalogDownloadService;
import dev.zerojdk.domain.service.CatalogUnchangedException;
import picocli.CommandLine;
import lombok.RequiredArgsConstructor;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@CommandLine.Command(name = "zjdk-update", description = "Update the catalog")
@RequiredArgsConstructor
public class ZjdkUpdate implements Runnable {
    private final CatalogMetadataRepository catalogMetadataRepository;
    private final CatalogDownloadService catalogDownloadService;

    @Override
    public void run() {
        try {
            String currentVersion = catalogMetadataRepository.getCurrentVersion()
                .map(CatalogMetadata::version)
                .orElse(null);

            Catalog catalog = catalogDownloadService.downloadLatestIfNewer(currentVersion);
            CatalogMetadata catalogMetadata = catalogMetadataRepository.setCurrentVersion(catalog.version());

            Files.move(catalog.location(), catalogMetadata.location(), StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Catalog updated.");
        } catch (CatalogUnchangedException e) {
            System.out.println("Catalog is already up-to-date.");
        } catch (Exception e) {
            System.out.println("Failed to update catalog: " + e.getMessage());
        }
    }
}
