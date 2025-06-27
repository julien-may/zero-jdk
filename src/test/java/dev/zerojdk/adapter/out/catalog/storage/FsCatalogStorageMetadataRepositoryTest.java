package dev.zerojdk.adapter.out.catalog.storage;

import dev.zerojdk.domain.model.catalog.storage.CatalogStorage;
import dev.zerojdk.domain.port.out.catalog.CatalogStorageLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FsCatalogStorageMetadataRepositoryTest {
    @TempDir
    private File tempDir;

    private Path metadataFile;
    private Path catalogFile;

    private FsCatalogStorageMetadataRepository repository;

    @BeforeEach
    void setup() {
        metadataFile = new File(tempDir, "catalog.properties").toPath();
        catalogFile = new File(tempDir, "catalog.json").toPath();

        CatalogStorageLayout storageLayout = mock(CatalogStorageLayout.class);

        when(storageLayout.metadataFile())
            .thenReturn(metadataFile);
        when(storageLayout.catalogFile())
            .thenReturn(catalogFile);

        repository = new FsCatalogStorageMetadataRepository(storageLayout);
    }

    @Test
    void getCurrentVersionReturnsEmptyIfFileDoesNotExist() {
        // When / Then
        assertThat(repository.getCurrentVersion())
            .isEmpty();
    }

    @Test
    void getCurrentVersion_reads_existing_metadata_file() throws Exception {
        // Given
        Properties properties = new Properties();
        properties.setProperty("version", "42.0.1");

        try (var out = Files.newOutputStream(metadataFile)) {
            properties.store(out, "Catalog metadata");
        }

        // When
        Optional<CatalogStorage> actual = repository.getCurrentVersion();

        // Then
        assertThat(actual)
            .map(CatalogStorage::version)
            .hasValue("42.0.1");

        assertThat(actual)
            .map(CatalogStorage::location)
            .hasValue(catalogFile);
    }

    @Test
    void setCurrentVersionWritesMetadataFileAndReturnsStorage() throws Exception {
        // When
        CatalogStorage actual = repository.setCurrentVersion("99.9.9");

        assertThat(actual.location())
            .isEqualTo(catalogFile);
        assertThat(actual.version())
            .isEqualTo("99.9.9");

        Properties properties = new Properties();
        try (var in = Files.newInputStream(metadataFile)) {
            properties.load(in);
        }

        assertThat(properties.getProperty("version"))
            .isEqualTo("99.9.9");
    }
}