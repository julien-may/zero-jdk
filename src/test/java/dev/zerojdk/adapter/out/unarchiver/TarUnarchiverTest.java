package dev.zerojdk.adapter.out.unarchiver;

import dev.zerojdk.adapter.out.unarchiver.compression.GzipCompression;
import dev.zerojdk.adapter.out.unarchiver.compression.NoCompression;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;

class TarUnarchiverTest {
    @TempDir
    private Path tempDir;

    private MockedStatic<PosixPermissions> mockedPosixPermissions;

    @BeforeEach
    void setupPosixPermissions() {
        mockedPosixPermissions = Mockito.mockStatic(PosixPermissions.class);
    }

    @AfterEach
    void resetPosixPermissions() {
        mockedPosixPermissions.close();
    }

    @Test
    @DisplayName("should extract files from a .tar.gz archive")
    void shouldExtractFromCompressedArchive() throws IOException {
        // Given
        ArchiveEntry file1 = ArchiveEntry.file("file1.txt", "Content of file 1");
        ArchiveEntry file2 = ArchiveEntry.file("file2.txt", "Content of file 2");

        // When
        Path extractedPath = new TarUnarchiver(
            createTarArchive(true, file1,  file2),
            new GzipCompression()).extract(tempDir);

        // Then
        assertThat(extractedPath)
            .isEqualTo(tempDir);
        assertThat(tempDir.resolve(file1.name()))
            .exists()
            .hasContent(file1.content());
        assertThat(tempDir.resolve(file2.name()))
            .exists()
            .hasContent(file2.content());

        mockedPosixPermissions.verify(() ->
            PosixPermissions.setPosixFilePermissions(any(Path.class), anyInt()), times(2));
    }

    @Test
    @DisplayName("should extract files from a .tar archive")
    void shouldExtractFilesFromUncompressedArchive() throws IOException {
        // Given
        ArchiveEntry file1 = ArchiveEntry.file("file1.txt", "Content of file 1");

        // When
        Path extractedPath = new TarUnarchiver(
            createTarArchive(false, file1),
            new NoCompression()).extract(tempDir);

        // Then
        assertThat(extractedPath)
            .isEqualTo(tempDir);
        assertThat(tempDir.resolve(file1.name()))
            .exists()
            .hasContent(file1.content());

        mockedPosixPermissions.verify(() ->
            PosixPermissions.setPosixFilePermissions(any(Path.class), anyInt()), times(1));
    }

    @Test
    @DisplayName("should extract files from a .tar.gz archive with nested directories")
    void shouldExtractFilesFromTarGzWithNestedDirectories() throws IOException {
        // Given
        Path archivePath = createTarArchive(true,
            ArchiveEntry.directory("root"),
            ArchiveEntry.directory("root/child"),
            ArchiveEntry.file("root/child/file1.txt", "Content of deeply nested file.")
        );

        // When
        Path extractedPath = new TarUnarchiver(archivePath,
            new GzipCompression()).extract(tempDir);

        // Then
        assertThat(extractedPath)
            .isEqualTo(tempDir);
        assertThat(tempDir.resolve("child", "file1.txt"))
            .exists()
            .hasContent("Content of deeply nested file.");

        mockedPosixPermissions.verify(() ->
            PosixPermissions.setPosixFilePermissions(any(Path.class), anyInt()), times(2)); // For two dirs and one file
    }

    @Test
    @DisplayName("should handle archive with no common root directory")
    void shouldHandleArchiveWithNoCommonRootDirectory() throws IOException {
        // Given
        ArchiveEntry file1 = ArchiveEntry.file("file1.txt", "Content of file 1");
        ArchiveEntry directory = ArchiveEntry.directory("child/");
        ArchiveEntry file2 = ArchiveEntry.file("child/file2.txt", "Content of file 2");

        Path archivePath = createTarArchive(true, file1, directory, file2);

        // When
        Path extractedPath = new TarUnarchiver(archivePath,
            new GzipCompression()).extract(tempDir);

        // Then
        assertThat(extractedPath)
            .isEqualTo(tempDir);
        assertThat(tempDir.resolve(file1.name()))
            .exists()
            .hasContent(file1.content());
        assertThat(tempDir.resolve(file2.name()))
            .exists()
            .hasContent(file2.content());

        mockedPosixPermissions.verify(() ->
            PosixPermissions.setPosixFilePermissions(any(Path.class), anyInt()), times(3));
    }

    record ArchiveEntry(String name, String content) {
        static ArchiveEntry file(String name, String content) {
            return new ArchiveEntry(name, content);
        }

        static ArchiveEntry directory(String name) {
            if (!name.endsWith("/")) {
                name = name + '/';
            }

            return new ArchiveEntry(name, null);
        }

        boolean isDirectory() {
            return content ==  null;
        }
    }

    private Path createTarArchive(boolean gzipped, ArchiveEntry... entries) throws IOException {
        Path archivePath = tempDir.resolve("archive.xyz");

        OutputStream outputStream =
            new BufferedOutputStream(Files.newOutputStream(archivePath));

        if (gzipped) {
            outputStream = new GzipCompressorOutputStream(outputStream);
        }

        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(outputStream)) {
            for (ArchiveEntry archiveEntry : entries) {
                TarArchiveEntry entry = new TarArchiveEntry(archiveEntry.name());

                if (!archiveEntry.isDirectory()) {
                    entry.setSize(archiveEntry.content().getBytes().length);
                }

                tos.putArchiveEntry(entry);

                if (archiveEntry.content != null) {
                    tos.write(archiveEntry.content.getBytes());
                }

                tos.closeArchiveEntry();
            }
        }

        return archivePath;
    }
}