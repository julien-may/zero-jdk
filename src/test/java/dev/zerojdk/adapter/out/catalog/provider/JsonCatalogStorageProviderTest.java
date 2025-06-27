package dev.zerojdk.adapter.out.catalog.provider;

import dev.zerojdk.domain.model.catalog.storage.CatalogStorage;
import dev.zerojdk.domain.service.catalog.CatalogStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JsonCatalogStorageProviderTest {
    @Mock
    private CatalogStorageService catalogStorageService;
    @InjectMocks
    private JsonCatalogStorageProvider provider;

    @Test
    void provideReturnsExistingStorageIfPresent() {
        // Given
        CatalogStorage storage = new CatalogStorage("v1", Path.of("/catalog/existing.json"));

        when(catalogStorageService.findCatalogStorage())
            .thenReturn(Optional.of(storage));

        // When
        CatalogStorage result = provider.provide();

        // Then
        assertThat(result).isEqualTo(storage);
        verify(catalogStorageService, never()).updateCatalog();
    }

    @Test
    void provideUpdatesCatalogIfNotPresent() {
        // Given
        CatalogStorage storage = new CatalogStorage("v1", Path.of("/catalog/new.json"));

        when(catalogStorageService.findCatalogStorage())
            .thenReturn(Optional.empty());
        when(catalogStorageService.updateCatalog())
            .thenReturn(storage);

        // When
        CatalogStorage result = provider.provide();

        // Then
        assertThat(result).isEqualTo(storage);
        verify(catalogStorageService).updateCatalog();
    }
}