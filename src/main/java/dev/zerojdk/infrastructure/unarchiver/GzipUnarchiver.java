package dev.zerojdk.infrastructure.unarchiver;

import dev.zerojdk.infrastructure.unarchiver.compression.GzipCompression;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RequiredArgsConstructor
public class GzipUnarchiver implements Unarchiver {
    private final Path archive;

    @SneakyThrows
    @Override
    public Path extract(Path target) {
        try (FileInputStream fileInputStream = new FileInputStream(archive.toFile());
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             InputStream inputStream = new GzipCompression().decompress(bufferedInputStream)) {

            Path targetFile = target.resolve(removeExtension(archive).getFileName());

            Files.copy(inputStream,
                targetFile,
                StandardCopyOption.REPLACE_EXISTING);

            return targetFile;
        }
    }

    private Path removeExtension(Path path) {
        int dotIndex = path.getFileName()
            .toString()
            .lastIndexOf('.');

        return (dotIndex == -1)
            ? path
            : path.resolveSibling(path.getFileName()
            .toString()
            .substring(0, dotIndex));
    }
}
