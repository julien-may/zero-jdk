package dev.zerojdk.adapter.in.cli;

import dev.zerojdk.domain.service.CatalogStorageService;
import dev.zerojdk.domain.service.CatalogUnchangedException;
import picocli.CommandLine;
import lombok.RequiredArgsConstructor;

@CommandLine.Command(header = "Update the catalog")
@RequiredArgsConstructor
public class ZjdkUpdate implements Runnable {
    private final CatalogStorageService catalogStorageService;

    @Override
    public void run() {
        try {
            catalogStorageService.updateCatalogIfNewer();
            System.out.println("Catalog updated.");
        } catch (CatalogUnchangedException e) {
            System.out.println("Catalog is already up-to-date.");
        } catch (Exception e) {
            System.out.println("Failed to update catalog: " + e.getMessage());
        }
    }
}
