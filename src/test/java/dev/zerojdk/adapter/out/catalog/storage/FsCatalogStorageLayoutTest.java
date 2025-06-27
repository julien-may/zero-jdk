package dev.zerojdk.adapter.out.catalog.storage;

import dev.zerojdk.domain.port.out.BaseLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FsCatalogStorageLayoutTest {
    @TempDir
    private File tempDir;

    private FsCatalogStorageLayout storageLayout;

    @BeforeEach
    void setup() {
        BaseLayout baseLayout = mock(BaseLayout.class);

        when(baseLayout.baseDirectory(true))
            .thenReturn(tempDir.toPath());
        when(baseLayout.ensureBaseDirectory(true))
            .thenReturn(tempDir.toPath());

        storageLayout = new FsCatalogStorageLayout(baseLayout);
    }

    @Test
    void ensureCatalogStorageDirectory() throws IOException {
        // Given
        assertThat(storageLayout.metadataFile().getParent())
            .doesNotExist();

        // When
        storageLayout.ensureCatalogStorageDirectory();

        // Then
        assertThat(storageLayout.metadataFile().getParent())
            .exists();
    }

    @Test
    void metadataFileReturnsPathInsideCatalogDirectory() {
        // When
        Path metadataFile = storageLayout.metadataFile();

        // Then
        assertThat(metadataFile)
            .hasFileName("catalog.properties");
        assertThat(metadataFile.getParent())
            .hasFileName("catalog");
    }

    @Test
    void catalogFileReturnsPathInsideCatalogDirectory() {
        // When
        Path catalogFile = storageLayout.catalogFile();

        // Then
        assertThat(catalogFile)
            .hasFileName("catalog.json");
        assertThat(catalogFile.getParent())
            .hasFileName("catalog");
    }
}