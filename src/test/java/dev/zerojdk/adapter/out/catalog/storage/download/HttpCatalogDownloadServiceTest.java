package dev.zerojdk.adapter.out.catalog.storage.download;

import dev.zerojdk.adapter.out.github.client.GitHubReleaseClient;
import dev.zerojdk.adapter.out.github.client.model.Asset;
import dev.zerojdk.adapter.out.github.client.model.Release;
import dev.zerojdk.domain.model.catalog.Catalog;
import dev.zerojdk.domain.port.out.unarchiving.Unarchiver;
import dev.zerojdk.domain.port.out.unarchiving.UnarchiverFactory;
import dev.zerojdk.domain.service.catalog.CatalogUnchangedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpCatalogDownloadServiceTest {
    @Mock
    private GitHubReleaseClient releaseClient;
    @Mock
    private UnarchiverFactory unarchiverFactory;
    @InjectMocks
    private HttpCatalogDownloadService service;

    @TempDir
    Path tempDir;

    private Unarchiver createUnarchiver(File archive) {
        Unarchiver unarchiver = mock(Unarchiver.class);
        when(unarchiverFactory.create(archive))
            .thenReturn(unarchiver);

        return unarchiver;
    }

    @Test
    void downloadLatestReturnsCatalogFromLatestRelease() {
        // Given
        Asset asset = new Asset("https://foo.zip");
        File archive = new File("foo.zip");

        File extracted = mock(File.class);
        when(extracted.isFile()).thenReturn(true);
        Path catalogPath = mock(Path.class);
        when(catalogPath.toFile()).thenReturn(extracted);

        when(releaseClient.getLatestRelease(any()))
            .thenReturn(new Release("v1.2.3", List.of(asset)));
        when(releaseClient.downloadReleaseAsset(asset))
            .thenReturn(archive);

        Unarchiver unarchiver = createUnarchiver(archive);

        when(unarchiver.extract(any()))
            .thenReturn(catalogPath);

        // When
        Catalog result = service.downloadLatest();

        // Then
        assertThat(result.version())
            .isEqualTo("v1.2.3");
        assertThat(result.location())
            .isEqualTo(catalogPath);
    }

    @Test
    void downloadThrowsWhenExtractedPathIsNotAFile() {
        // Given
        Asset asset = new Asset("https://foo.zip");
        File archive = new File("foo.zip");

        when(releaseClient.getLatestRelease(any()))
            .thenReturn(new Release("v9.9.9", List.of(asset)));
        when(releaseClient.downloadReleaseAsset(asset))
            .thenReturn(archive);

        Unarchiver unarchiver = createUnarchiver(archive);

        when(unarchiver.extract(any()))
            .thenReturn(tempDir);

        // Then
        assertThatThrownBy(() -> service.downloadLatest())
            .isInstanceOf(RuntimeException.class)
            .hasMessageEndingWith("is not a file");
    }

    @Test
    void downloadLatestIfNewerThrowsWhenSameVersion() {
        // Given
        Asset asset = new Asset("https://foo.zip");

        Release release = new Release("v1.2.3", List.of(asset));
        when(releaseClient.getLatestRelease(any()))
            .thenReturn(release);

        // Then / Then
        assertThatThrownBy(() -> service.downloadLatestIfNewer("v1.2.3"))
            .isInstanceOf(CatalogUnchangedException.class);
    }

    @Test
    void downloadThrowsIfNoAssetFound() {
        // Given
        Release release = new Release("v1.0.0", List.of());
        when(releaseClient.getLatestRelease(any())).thenReturn(release);

        // Then
        assertThatThrownBy(() -> service.downloadLatest())
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Asset not found");
    }
}