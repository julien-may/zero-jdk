package dev.zerojdk.adapter.out.catalog;

import dev.zerojdk.domain.port.out.catalog.CatalogMetadata;
import dev.zerojdk.domain.port.out.catalog.CatalogMetadataRepository;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public class FsCatalogMetadataRepository implements CatalogMetadataRepository {
    private final Path foo = Path.of(System.getProperty("user.home"), ".zjdk");
    private final Path metadataFile = foo.resolve("catalog.properties");
    private final Path catalogFile = foo.resolve("catalog.json");

    @SneakyThrows
    @Override
    public Optional<CatalogMetadata> getCurrentVersion() {
        if (!Files.exists(metadataFile)) {
            return Optional.empty();
        }

        Properties props = new Properties();
        try (var in = Files.newInputStream(metadataFile)) {
            props.load(in);
        }

        return Optional.of(new CatalogMetadata(
            props.getProperty("version"),
            catalogFile));
    }

    @SneakyThrows
    @Override
    public CatalogMetadata setCurrentVersion(String version) {
        Properties props = new Properties();
        props.setProperty("version", version);

        Files.createDirectories(metadataFile.getParent());

        try (var out = Files.newOutputStream(metadataFile)) {
            props.store(out, "Catalog metadata");
        }

        return new CatalogMetadata(version, catalogFile);
    }
}