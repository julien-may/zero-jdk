package dev.zerojdk.adapter.out.catalog.storage;

import dev.zerojdk.domain.model.catalog.storage.CatalogStorage;
import dev.zerojdk.domain.port.out.catalog.CatalogStorageMetadataRepository;
import dev.zerojdk.domain.port.out.catalog.CatalogStorageLayout;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

@RequiredArgsConstructor
public class FsCatalogStorageMetadataRepository implements CatalogStorageMetadataRepository {
    private final CatalogStorageLayout catalogStorageLayout;

    @SneakyThrows
    @Override
    public Optional<CatalogStorage> getCurrentVersion() {
        Path metadataFile = catalogStorageLayout.metadataFile();

        if (!Files.exists(metadataFile)) {
            return Optional.empty();
        }

        Properties props = new Properties();
        try (var in = Files.newInputStream(metadataFile)) {
            props.load(in);
        }

        return Optional.of(new CatalogStorage(
            props.getProperty("version"),
            catalogStorageLayout.catalogFile()));
    }

    @SneakyThrows
    @Override
    public CatalogStorage setCurrentVersion(String version) {
        Properties props = new Properties();
        props.setProperty("version", version);

        Path metadataFile = catalogStorageLayout.metadataFile();

        try (var out = Files.newOutputStream(metadataFile)) {
            props.store(out, "Catalog metadata");
        }

        return new CatalogStorage(version, catalogStorageLayout.catalogFile());
    }
}