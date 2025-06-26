package dev.zerojdk.adapter.out.unarchiver;

import dev.zerojdk.domain.port.out.unarchiving.Unarchiver;
import dev.zerojdk.domain.port.out.unarchiving.compression.Compression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class TarUnarchiver implements Unarchiver {
    private final Path archive;
    @Getter
    private final Compression compression;

    @Override
    @SneakyThrows
    public Path extract(Path destination) {
        String rootDirName = findCommonRootDirectory(archive.toFile(), compression);

        List<Runnable> deferredSymlinks = new ArrayList<>();

        try (TarArchiveInputStream tar = new TarArchiveInputStream(compression.decompress(
            new BufferedInputStream(new FileInputStream(archive.toFile()))))) {

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
                    PosixPermissions.setPosixFilePermissions(target, entry.getMode());
                } else if (entry.isSymbolicLink() || entry.isLink()) {
                    deferredSymlinks.add(createLink(entry, target, destination));
                } else {
                    extractFile(tar, entry, target);
                }
            }
        }

        deferredSymlinks.forEach(Runnable::run);
        return destination;
    }

    private String normalizeEntryName(String name, String rootDirName) {
        if (rootDirName != null) {
            Path namePath = Path.of(name).normalize();
            Path rootPath = Path.of(rootDirName);

            if (namePath.equals(rootPath)) {
                return null;
            }

            if (namePath.startsWith(rootDirName + "/")) {
                return rootPath.relativize(namePath).toString();
            }
        }

        return name;
    }

    private Runnable createLink(TarArchiveEntry entry, Path target, Path destination) {
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

    private void extractFile(TarArchiveInputStream tar, TarArchiveEntry entry, Path target) throws IOException {
        Files.createDirectories(target.getParent());

        try (OutputStream outputStream = Files.newOutputStream(target)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            long remaining = entry.getSize();

            while (remaining > 0 && (bytesRead = tar.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
        }

        PosixPermissions.setPosixFilePermissions(target, entry.getMode());
    }

    private String findCommonRootDirectory(File archive, Compression compression) throws IOException {
        String commonRootDirectory = null;
        boolean multipleTopLevelEntries = false;

        try (TarArchiveInputStream tar = new TarArchiveInputStream(compression.decompress(
            new BufferedInputStream(new FileInputStream(archive))))) {

            TarArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                String name = entry.getName();
                String topLevelEntry = name.split("/", 2)[0];

                // Check if the current entry is a top-level directory itself
                boolean isTopLevelDirectoryEntry = entry.isDirectory()
                    && (name.equals(topLevelEntry) || name.equals(topLevelEntry + "/"));

                if (commonRootDirectory == null) {
                    if (isTopLevelDirectoryEntry) {
                        commonRootDirectory = topLevelEntry;
                    } else {
                        multipleTopLevelEntries = true;
                        break;
                    }
                } else {
                    if (!commonRootDirectory.equals(topLevelEntry)) {
                        multipleTopLevelEntries = true;
                        break;
                    }
                }
            }
        }

        return  multipleTopLevelEntries || commonRootDirectory == null
            ? null
            : commonRootDirectory;
    }
}
