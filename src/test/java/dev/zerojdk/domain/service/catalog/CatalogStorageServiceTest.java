package dev.zerojdk.domain.service.catalog;

import dev.zerojdk.domain.model.catalog.Catalog;
import dev.zerojdk.domain.model.catalog.storage.CatalogStorage;
import dev.zerojdk.domain.port.out.catalog.CatalogDownloadService;
import dev.zerojdk.domain.port.out.catalog.CatalogStorageMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogStorageServiceTest {
    @Mock
    private CatalogDownloadService downloadService;
    @Mock
    private CatalogStorageMetadataRepository metadataRepository;

    @InjectMocks
    private CatalogStorageService service;

    @TempDir
    Path tempDir;

    private Path tempSource;
    private Path finalTarget;

    @BeforeEach
    void setUp() throws Exception {
        tempSource = Files.writeString(tempDir.resolve("catalog-temp.json"), "catalog-data");
        finalTarget = tempDir.resolve("catalog.json");
    }

    @Test
    void updateCatalogDownloadsAndStoresTheCatalog() {
        // Given
        Catalog catalog = new Catalog("v1", tempSource);
        CatalogStorage storage = new CatalogStorage("v1", finalTarget);

        when(downloadService.downloadLatest())
            .thenReturn(catalog);
        when(metadataRepository.setCurrentVersion("v1"))
            .thenReturn(storage);

        // When
        CatalogStorage actual = service.updateCatalog();

        // Then
        assertThat(actual)
            .isEqualTo(storage);
        assertThat(finalTarget)
            .exists();
        assertThat(tempSource)
            .doesNotExist();
    }

    @Test
    void updateCatalogIfNewerSkipsUpdateIfAlreadyUpToDate() {
        // Given
        when(metadataRepository.getCurrentVersion())
            .thenReturn(Optional.of(new CatalogStorage("v1", Path.of("ignored"))));
        doThrow(new CatalogUnchangedException("up-to-date"))
            .when(downloadService).downloadLatestIfNewer("v1");

        // Then
        assertThatThrownBy(() -> service.updateCatalogIfNewer())
            .isInstanceOf(CatalogUnchangedException.class);
    }

    @Test
    void updateCatalogIfNewerDownloadsAndUpdatesTheCatalog() {
        // Given
        when(metadataRepository.getCurrentVersion())
            .thenReturn(Optional.of(new CatalogStorage("v1", Path.of("old"))));

        Catalog catalog = new Catalog("v2", tempSource);
        CatalogStorage storage = new CatalogStorage("v2", finalTarget);

        when(downloadService.downloadLatestIfNewer("v1"))
            .thenReturn(catalog);
        when(metadataRepository.setCurrentVersion("v2"))
            .thenReturn(storage);

        // When
        CatalogStorage actual = service.updateCatalogIfNewer();

        // Then
        assertThat(actual.version())
            .isEqualTo("v2");
        assertThat(finalTarget)
            .exists();
    }

    @Test
    void findCatalogStorageReturnsTheCatalog() {
        // Given
        CatalogStorage storage = new CatalogStorage("vX", finalTarget);
        when(metadataRepository.getCurrentVersion())
            .thenReturn(Optional.of(storage));

        // When
        Optional<CatalogStorage> actual = service.findCatalogStorage();

        // Then
        assertThat(actual)
            .hasValue(storage);
    }

    @Test
    void updateCatalogThrowsOnFileMoveFailure() {
        // Given
        Path nonExistentPath = tempDir.resolve("nonexistent.json");
        Catalog catalog = new Catalog("v1", nonExistentPath);

        when(downloadService.downloadLatest())
            .thenReturn(catalog);

        CatalogStorage storage = new CatalogStorage("v1", finalTarget);
        when(metadataRepository.setCurrentVersion("v1"))
            .thenReturn(storage);

        // Then
        assertThatThrownBy(() -> service.updateCatalog())
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to move catalog file");
    }
}
