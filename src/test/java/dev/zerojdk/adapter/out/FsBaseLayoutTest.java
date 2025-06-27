package dev.zerojdk.adapter.out;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith({MockitoExtension.class, SystemStubsExtension.class})
class FsBaseLayoutTest {
    private final FsBaseLayout baseLayout = new FsBaseLayout();

    @SystemStub
    private SystemProperties systemProperties;

    @TempDir
    private Path tempDir;

    private Path homeDir;

    @BeforeEach
    void setup() {
        homeDir = tempDir.resolve("home");
        systemProperties.set("user.home", homeDir.toString());
    }

    @Test
    void discoverProjectRootReturnsEmptyIfNoZjdkDirectoryAnywhere() {
        // Given
        Path project = tempDir.resolve("project");

        try (MockedStatic<Path> path = Mockito.mockStatic(Path.class, Mockito.CALLS_REAL_METHODS)) {
            path.when(() -> Path.of("."))
                .thenReturn(project);

            // When / Then
            assertThat(baseLayout.discoverProjectRoot()).isEmpty();
        }
    }

    @Test
    void discoverProjectRootReturnsEmptyIfOnlyInHomeDirectory() throws Exception {
        // Given
        Path project = tempDir.resolve("project");
        Files.createDirectories(homeDir.resolve(".zjdk"));

        try (MockedStatic<Path> path = Mockito.mockStatic(Path.class, Mockito.CALLS_REAL_METHODS)) {
            path.when(() -> Path.of("."))
                .thenReturn(project);

            // When / Then
            assertThat(baseLayout.discoverProjectRoot()).isEmpty();
        }
    }

    @Test
    void discoverProjectRootReturnsCurrentDirIfZjdkExistsThere() throws Exception {
        // Given
        Path project = tempDir.resolve("project");
        Files.createDirectories(project.resolve(".zjdk"));

        try (MockedStatic<Path> path = Mockito.mockStatic(Path.class, Mockito.CALLS_REAL_METHODS)) {
            path.when(() -> Path.of("."))
                .thenReturn(project);

            // When / Then
            assertThat(baseLayout.discoverProjectRoot())
                .contains(project);
        }
    }

    @Test
    void discoverProjectRootReturnsParentDirIfZjdkExistsThere() throws Exception {
        // Given
        Path root = tempDir.resolve("project");
        Files.createDirectories(root.resolve(".zjdk"));

        Path subdir = root.resolve("subdir");
        Files.createDirectories(subdir);

        try (MockedStatic<Path> path = Mockito.mockStatic(Path.class, Mockito.CALLS_REAL_METHODS)) {
            path.when(() -> Path.of("."))
                .thenReturn(subdir);

            // When / Then
            assertThat(baseLayout.discoverProjectRoot())
                .contains(root);
        }
    }

    @Test
    void configFileShouldReturnGlobalPath() {
        // When
        Path actual = baseLayout.configFile(true);

        // Then
        assertThat(actual.toString())
            .startsWith(homeDir.toString())
            .endsWith(".zjdk/config.properties");
    }

    @Test
    void configFileShouldReturnLocalPath() throws Exception {
        // Given
        Path project = tempDir.resolve("project");
        Files.createDirectories(project.resolve(".zjdk"));

        try (MockedStatic<Path> path = Mockito.mockStatic(Path.class, Mockito.CALLS_REAL_METHODS)) {
            path.when(() -> Path.of("."))
                .thenReturn(project);

            // When
            Path actual = baseLayout.configFile(false);

            // Then
            assertThat(actual.toString())
                .startsWith(project.toString())
                .endsWith(".zjdk/config.properties");
        }
    }

    @Test
    void configFileShouldThrowIfNotManage() {
        // Given
        Path project = tempDir.resolve("project");

        try (MockedStatic<Path> path = Mockito.mockStatic(Path.class, Mockito.CALLS_REAL_METHODS)) {
            path.when(() -> Path.of("."))
                .thenReturn(project);

            // When
            assertThatThrownBy(() -> baseLayout.configFile(false))
                .isInstanceOf(UnmanagedDirectoryException.class);
        }
    }

    @Test
    void ensureBaseDirectoryShouldCreateGlobal() {
        // Given
        Path baseDirectory = homeDir.resolve(".zjdk");
        assertThat(baseDirectory).doesNotExist();

        // When
        baseLayout.ensureBaseDirectory(true);

        // Then
        assertThat(baseDirectory)
            .exists()
            .isDirectory();
    }

    @Test
    void ensureBaseDirectoryShouldCreateLocal() throws Exception {
        Path project = tempDir.resolve("project");
        Files.createDirectories(project);

        Path baseDirectory = project.resolve(".zjdk");
        assertThat(baseDirectory).doesNotExist();

        try (MockedStatic<Path> path = Mockito.mockStatic(Path.class, Mockito.CALLS_REAL_METHODS)) {
            path.when(() -> Path.of("."))
                .thenReturn(project);

            // When
            baseLayout.ensureBaseDirectory(false);

            // Then
            assertThat(baseDirectory)
                .exists()
                .isDirectory();
        }
    }
}
