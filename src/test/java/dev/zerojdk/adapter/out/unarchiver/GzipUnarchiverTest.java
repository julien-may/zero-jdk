package dev.zerojdk.adapter.out.unarchiver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class GzipUnarchiverTest {
    @TempDir
    private Path tempDir;

    @ParameterizedTest
    @CsvSource({
        "foo.gz, foo",
        "foo.text.gz, foo.text",
        ".foo.gz, .foo"
    })
    void shouldExtractGzFileCorrectly(Path archiveName, String extractedName) throws IOException {
        // Given
        String content = "This is a test string for GzipUnarchiver.";

        File archive = createGzipFile(
            tempDir.resolve(archiveName),
            content);

        GzipUnarchiver unarchiver = new GzipUnarchiver(archive.toPath());

        // When
        Path extractedPath = unarchiver.extract(tempDir);

        // Then
        assertThat(extractedPath)
            .exists()
            .hasFileName(extractedName);

        assertThat(Files.readString(extractedPath))
            .isEqualTo(content);
    }

    private File createGzipFile(Path archivePath, String content) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(archivePath.toFile());
             GZIPOutputStream gos = new GZIPOutputStream(fos)) {
            gos.write(content.getBytes());
        }

        return archivePath.toFile();
    }
}

