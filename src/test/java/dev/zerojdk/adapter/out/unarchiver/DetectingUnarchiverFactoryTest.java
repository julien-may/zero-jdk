package dev.zerojdk.adapter.out.unarchiver;

import dev.zerojdk.domain.port.out.unarchiving.Unarchiver;

import dev.zerojdk.adapter.out.unarchiver.compression.GzipCompression;
import dev.zerojdk.adapter.out.unarchiver.compression.NoCompression;
import dev.zerojdk.domain.port.out.unarchiving.compression.Compression;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DetectingUnarchiverFactoryTest {
    private final DetectingUnarchiverFactory factory = new DetectingUnarchiverFactory();

    @TempDir
    private Path tempDir;
    
    static Stream<Arguments> provideTarUnarchiverArguments() {
        return Stream.of(
            Arguments.of("tar", NoCompression.class),
            Arguments.of("tgz", GzipCompression.class),
            Arguments.of("tar.gz", GzipCompression.class)
        );
    }

    @ParameterizedTest
    void shouldReturnTarUnarchiver(String fileExtension, Class<? extends Compression> compressionType) {
        // Given
        File archive = tempDir.resolve("test." + fileExtension).toFile();

        // When
        Unarchiver unarchiver = factory.create(archive);

        // Then
        assertThat(unarchiver)
            .isInstanceOf(TarUnarchiver.class);

        TarUnarchiver tarUnarchiver = (TarUnarchiver) unarchiver;

        assertThat(tarUnarchiver.getCompression())
            .isInstanceOf(compressionType);
    }

    @Test
    void shouldReturnGzipUnarchiverForGz() throws IOException {
        // Given
        File archive = tempDir.resolve("test.gz").toFile();
        assertThat(archive.createNewFile()).isTrue();

        // When
        Unarchiver unarchiver = factory.create(archive);

        // Then
        assertThat(unarchiver)
            .isInstanceOf(GzipUnarchiver.class);
    }

    @Test
    void shouldThrowUnsupportedArchiveExceptionForUnsupportedFileType() throws IOException {
        // Given
        File archive = tempDir.resolve("test.zip").toFile();
        assertThat(archive.createNewFile()).isTrue();

        // When / Then
        assertThatThrownBy(() -> factory.create(archive))
            .isInstanceOf(UnsupportedArchiveException.class)
            .hasMessage("Unsupported archive: test.zip");
    }
}