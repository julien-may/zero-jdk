package dev.zerojdk.domain.service.config;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.model.OperatingSystem;
import dev.zerojdk.domain.model.ProcessorArchitecture;
import dev.zerojdk.domain.port.out.config.JdkConfigRepository;
import dev.zerojdk.domain.service.catalog.CatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JdkConfigServiceTest {

    @Mock
    private JdkConfigRepository jdkConfigRepository;

    @Mock
    private CatalogService catalogService;

    @InjectMocks
    private JdkConfigService service;

    private Platform platform;

    @BeforeEach
    void setUp() {
        platform = new Platform(OperatingSystem.LINUX, ProcessorArchitecture.AARCH64);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getActiveVersionReturnsVersionFromRepository(boolean global) {
        // Given
        when(jdkConfigRepository.readVersion(global))
            .thenReturn("21.0.2");

        // When
        String actual = service.getActiveVersion(global);

        // Then
        assertThat(actual).isEqualTo("21.0.2");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void updateConfigurationThrowsIfIdentifierNotSupported(boolean global) {
        // Given
        when(catalogService.findByIdentifier(platform, "invalid"))
            .thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> service.createConfiguration(platform, "invalid", global))
            .isInstanceOfSatisfying(UnsupportedIdentifierException.class,
                ex -> assertThat(ex.getIdentifier()).isEqualTo("invalid"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void updateConfigurationUpdatesRepositoryIfIdentifierExists(boolean global) {
        // Given
        when(catalogService.findByIdentifier(platform, "21.0.2"))
            .thenReturn(Optional.of(new JdkVersion()));

        // When
        service.updateConfiguration(platform, "21.0.2", global);

        // Then
        verify(jdkConfigRepository).update("21.0.2", global);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void createConfigurationWithExplicitIdentifierValidatesAndCreates(boolean global) {
        // Given
        when(catalogService.findByIdentifier(platform, "21.0.2"))
            .thenReturn(Optional.of(new JdkVersion()));

        // When
        service.createConfiguration(platform, "21.0.2", global);

        // Then
        verify(jdkConfigRepository).create("21.0.2", global);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void createConfigurationWithoutVersionUsesDefaultLtsFromTemurin(boolean global) {
        // Given
        JdkVersion jdkVersion = new JdkVersion();
        jdkVersion.setIdentifier("temurin-21");
        jdkVersion.setSupport(JdkVersion.Support.LTS);

        when(catalogService.findLatestByDistribution(platform, "Temurin"))
            .thenReturn(List.of(jdkVersion));
        when(catalogService.findByIdentifier(platform, "temurin-21"))
            .thenReturn(Optional.of(jdkVersion));

        // When
        service.createConfiguration(platform, null, global);

        // Then
        verify(jdkConfigRepository).create("temurin-21", global);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void createConfigurationWithoutVersionThrowsIfNoLtsVersionFound(boolean global) {
        // Given
        when(catalogService.findLatestByDistribution(platform, "Temurin"))
            .thenReturn(List.of());

        // Then
        assertThatThrownBy(() -> service.createConfiguration(platform, null, global))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("There was an issue resolving the default version");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void createConfigurationThrowsIfDefaultIdentifierInvalid(boolean global) {
        // Given
        JdkVersion jdkVersion = new JdkVersion();
        jdkVersion.setIdentifier("temurin-21");
        jdkVersion.setSupport(JdkVersion.Support.LTS);

        when(catalogService.findLatestByDistribution(platform, "Temurin"))
            .thenReturn(List.of(jdkVersion));
        when(catalogService.findByIdentifier(platform, "temurin-21"))
            .thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> service.createConfiguration(platform, null, global))
            .isInstanceOfSatisfying(UnsupportedIdentifierException.class,
                ex -> assertThat(ex.getIdentifier()).isEqualTo("temurin-21"));
    }
}
