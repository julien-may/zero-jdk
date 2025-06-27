package dev.zerojdk.adapter.out.catalog;

import dev.zerojdk.adapter.out.catalog.provider.CatalogStorageProvider;
import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.model.OperatingSystem;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.model.ProcessorArchitecture;
import dev.zerojdk.domain.model.catalog.storage.CatalogStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.*;

class JsonCatalogRepositoryTest {
    private JsonCatalogRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        URI catalogUri = JsonCatalogRepositoryTest.class.getResource("/catalog/catalog.json").toURI();

        CatalogStorageProvider catalogStorageProvider = mock(CatalogStorageProvider.class);
        when(catalogStorageProvider.provide())
            .thenReturn(new CatalogStorage("1.0.0", new File(catalogUri).toPath()));

        repository = new JsonCatalogRepository(catalogStorageProvider);
    }

    @Test
    void findAllReturnsAllEntriesGroupedByDistribution() {
        // When
        Map<String, List<JdkVersion>> actual = repository.findAll(
            new Platform(OperatingSystem.LINUX, ProcessorArchitecture.X64));

        // Then
        assertThat(actual)
            .containsOnlyKeys("Semeru", "Zulu");

        assertThat(actual.get("Semeru"))
            .extracting(JdkVersion::getIdentifier)
            .containsOnly("semeru-21.0.7", "semeru-22.0.1", "semeru-22.0.2");

        assertThat(actual.get("Zulu"))
            .extracting(JdkVersion::getIdentifier)
            .containsOnly("zulu-23.32.11");
    }

    @Test
    void findAllReturnsNone() {
        // When
        Map<String, List<JdkVersion>> actual = repository.findAll(
            new Platform(OperatingSystem.LINUX, ProcessorArchitecture.AARCH64));

        // Then
        assertThat(actual).isEmpty();
    }

    @Test
    void findAllByDistributionFiltersByDistribution() {
        // When
        List<JdkVersion> actual = repository.findAllByDistribution(
            new Platform(OperatingSystem.MACOS, ProcessorArchitecture.X64), "Corretto");

        // Then
        assertThat(actual)
            .extracting(JdkVersion::getIdentifier)
            .containsOnly("corretto-18.0.0.37.1");
    }

    @Test
    void findAllByDistributionReturnsNone() {
        // When
        List<JdkVersion> actual = repository.findAllByDistribution(
            new Platform(OperatingSystem.LINUX, ProcessorArchitecture.X64), "Corretto");

        // Then
        assertThat(actual).isEmpty();
    }

    @Test
    void findLatestReturnsLatestPerSupportAndDistribution() {
        // When
        Map<String, List<JdkVersion>> actual = repository.findLatest(
            new Platform(OperatingSystem.MACOS, ProcessorArchitecture.AARCH64));

        // Then
        assertThat(actual)
            .containsOnlyKeys("Corretto", "Semeru", "Zulu");

        assertThat(actual.get("Corretto"))
            .extracting(JdkVersion::getIdentifier)
            .containsOnly("corretto-22.0.2.9.1");

        assertThat(actual.get("Semeru"))
            .extracting(JdkVersion::getIdentifier, JdkVersion::getSupport)
            .containsOnly(
                tuple("semeru-21.0.7", JdkVersion.Support.LTS),
                tuple("semeru-22.0.2", JdkVersion.Support.NON_LTS));

        assertThat(actual.get("Zulu"))
            .extracting(JdkVersion::getIdentifier)
            .containsOnly("zulu-23.32.11");
    }

    @Test
    void findLatestByDistributionReturnsLatestPerSupport() {
        // When
        List<JdkVersion> actual = repository.findLatestByDistribution(
            new Platform(OperatingSystem.MACOS, ProcessorArchitecture.AARCH64), "Semeru");

        // Then
        assertThat(actual)
            .extracting(JdkVersion::getIdentifier, JdkVersion::getSupport)
            .containsOnly(
                tuple("semeru-21.0.7", JdkVersion.Support.LTS),
                tuple("semeru-22.0.2", JdkVersion.Support.NON_LTS));
    }

    @Test
    void findByIdentifierReturnsMatchingVersion() {
        // When
        Optional<JdkVersion> actual = repository.findByIdentifier(
            new Platform(OperatingSystem.MACOS, ProcessorArchitecture.AARCH64), "corretto-22.0.2.9.1");

        // Then
        assertThat(actual)
            .map(JdkVersion::getIdentifier)
            .contains("corretto-22.0.2.9.1");
    }

    @Test
    void findByIdentifierReturnsNone() {
        // When
        Optional<JdkVersion> actual = repository.findByIdentifier(
            new Platform(OperatingSystem.MACOS, ProcessorArchitecture.AARCH64), "foo");

        // Then
        assertThat(actual).isEmpty();
    }

}
