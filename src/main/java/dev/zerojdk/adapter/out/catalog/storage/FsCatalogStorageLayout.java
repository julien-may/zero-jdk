package dev.zerojdk.adapter.out.catalog.storage;

import dev.zerojdk.domain.port.out.BaseLayout;
import dev.zerojdk.domain.port.out.catalog.CatalogStorageLayout;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class FsCatalogStorageLayout implements CatalogStorageLayout {
    private final BaseLayout baseLayout;

    @Override
    public Path metadataFile() {
        return catalogStorageDirectory()
            .resolve("catalog.properties");
    }

    @Override
    public Path catalogFile() {
        return catalogStorageDirectory()
            .resolve("catalog.json");
    }

    @Override
    public Path ensureCatalogStorageDirectory() throws IOException {
        return Files.createDirectories(baseLayout.ensureBaseDirectory(true)
            .resolve("catalog"));
    }

    private Path catalogStorageDirectory() {
        return baseLayout.baseDirectory(true)
            .resolve("catalog");
    }
}
