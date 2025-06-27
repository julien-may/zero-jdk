package dev.zerojdk.adapter.out.config;

import dev.zerojdk.domain.port.out.BaseLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FsJdkConfigRepositoryTest {
    @Mock
    private BaseLayout baseLayout;

    @InjectMocks
    private FsJdkConfigRepository repository;

    @TempDir
    private File tempDir;

    private Path configFile;

    @BeforeEach
    void setup() {
        configFile = tempDir.toPath().resolve("config.properties");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void readVersionReturnsCorrectValue(boolean global) throws Exception {
        // Given
        storeVersion("21.0.2");

        when(baseLayout.configFile(global))
            .thenReturn(configFile);

        // When
        String actual = repository.readVersion(global);

        // Then
        assertThat(actual)
            .isEqualTo("21.0.2");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void updateWritesVersionToFile(boolean global) throws Exception {
        // Given
        when(baseLayout.configFile(global))
            .thenReturn(configFile);

        // When
        repository.update("17.0.10", global);

        // Then
        Properties properties = loadProperties();

        assertThat(properties.getProperty("version"))
            .isEqualTo("17.0.10");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void createFailsIfConfigFileAlreadyExists(boolean global) throws Exception {
        // Given
        storeVersion("20.0.0");

        when(baseLayout.configFile(global))
            .thenReturn(configFile);

        // When / Then
        assertThatThrownBy(() -> repository.create("21.0.1", global))
            .isInstanceOf(ConfigFileAlreadyExistsException.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void createWritesVersionIfFileDoesNotExist(boolean global) throws Exception {
        // Given
        when(baseLayout.configFile(global))
            .thenReturn(configFile);
        when(baseLayout.ensureBaseDirectory(global))
            .thenReturn(tempDir.toPath());

        // When
        repository.create("22.0.0", global);

        // Then
        Properties properties = loadProperties();

        assertThat(properties.getProperty("version"))
            .isEqualTo("22.0.0");
    }

    private void storeVersion(String version) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("version", version);

        try (var out = Files.newOutputStream(configFile)) {
            properties.store(out, null);
        }
    }

    private Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        try (var in = Files.newInputStream(configFile)) {
            properties.load(in);
        }

        return properties;
    }
}
