package dev.zerojdk.adapter.out.unarchiver;

import dev.zerojdk.domain.port.out.unarchiving.Unarchiver;
import dev.zerojdk.domain.port.out.unarchiving.compression.Compression;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@RequiredArgsConstructor
public class TarUnarchiver implements Unarchiver {
    private final Path archive;
    private final Compression compression;

    @Override
    @SneakyThrows
    public Path extract(Path destination) {
        String rootDirName = findCommonRootDirectory(archive.toFile());
        List<Runnable> deferredSymlinks = new ArrayList<>();

        try (TarArchiveInputStream tar = openTarStream(archive)) {
            TarArchiveEntry entry;

            while ((entry = tar.getNextEntry()) != null) {
                String entryName = normalizeEntryName(entry.getName(), rootDirName);

                if (entryName == null) {
                    continue;
                }

                Path target = destination.resolve(entryName).normalize();

                if (!target.startsWith(destination)) {
                    throw new IOException("Entry tries to escape target dir: " + target);
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else if (entry.isSymbolicLink() || entry.isLink()) {
                    deferredSymlinks.add(createLinkTask(entry, target, destination));
                } else {
                    extractFile(tar, entry, target);
                }
            }
        }

        deferredSymlinks.forEach(Runnable::run);
        return destination;
    }

    @SneakyThrows
    private TarArchiveInputStream openTarStream(Path file) {
        return new TarArchiveInputStream(compression.decompress(
            new BufferedInputStream(new FileInputStream(file.toFile()))));
    }

    private String normalizeEntryName(String name, String rootDirName) {
        if (rootDirName != null) {
            Path namePath = Path.of(name).normalize();

            if (namePath.equals(Path.of(rootDirName))) {
                return null;
            }

            if (namePath.startsWith(rootDirName + "/")) {
                return Path.of(rootDirName).relativize(namePath).toString();
            }
        }

        return name;
    }

    private Runnable createLinkTask(TarArchiveEntry entry, Path target, Path destination) {
        return () -> {
            try {
                Files.createDirectories(target.getParent());
                Files.deleteIfExists(target);

                if (entry.isSymbolicLink()) {
                    Files.createSymbolicLink(target, Paths.get(entry.getLinkName()));
                } else {
                    Files.createLink(target, destination.resolve(entry.getLinkName()).normalize());
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    @SneakyThrows
    private void extractFile(TarArchiveInputStream tar, TarArchiveEntry entry, Path target) {
        Files.createDirectories(target.getParent());
        Files.copy(tar, target, StandardCopyOption.REPLACE_EXISTING);

        PosixPermissions.setPosixFilePermissions(target, entry.getMode());
    }

    @SneakyThrows
    private static String findCommonRootDirectory(File archive) {
        try (TarArchiveInputStream tar = new TarArchiveInputStream(new GzipCompressorInputStream(
            new BufferedInputStream(new FileInputStream(archive))))) {

            String commonRoot = null;
            TarArchiveEntry entry;

            while ((entry = tar.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.startsWith("./")) {
                    name = name.substring(2);
                }

                if (name.isEmpty()) {
                    continue;
                }

                String top = name.split("/", 2)[0];

                if (commonRoot == null) {
                    commonRoot = top;
                } else if (!commonRoot.equals(top)) {
                    return null;
                }
            }

            return commonRoot;
        }
    }
}
