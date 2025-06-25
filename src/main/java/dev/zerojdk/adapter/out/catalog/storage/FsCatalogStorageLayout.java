package dev.zerojdk.adapter.out.catalog.storage;

import dev.zerojdk.domain.port.out.BaseLayout;
import dev.zerojdk.domain.port.out.catalog.CatalogStorageLayout;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class FsCatalogStorageLayout implements CatalogStorageLayout {
    private final BaseLayout baseLayout;

    @Override
    public Path metadataFile() {
        return ensureCatalogDirectory()
            .resolve("catalog.properties");
    }

    @Override
    public Path catalogFile() {
        return ensureCatalogDirectory()
            .resolve("catalog.json");
    }

    @SneakyThrows
    private Path ensureCatalogDirectory() {
        return Files.createDirectories(baseLayout.baseDirectory(true)
            .resolve("catalog"));
    }
}
