package dev.zerojdk.adapter.out.unarchiver;

import dev.zerojdk.domain.port.out.unarchiving.Unarchiver;
import dev.zerojdk.domain.port.out.unarchiving.compression.Compression;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

@RequiredArgsConstructor
public class TarUnarchiver implements Unarchiver {
    private final Path archive;
    private final Compression compression;

    @SneakyThrows
    @Override
    public Path extract(Path destination) {
        Optional<String> rootDirectory = findRootDirectory(archive.toFile());

        // we always want to extract into  destination/alternativeName
        Path extractRoot = destination;

        try (FileInputStream fileInputStream = new FileInputStream(archive.toFile())) {
            try (BufferedInputStream inputStream = new BufferedInputStream(fileInputStream);
                 TarArchiveInputStream tar = new TarArchiveInputStream(new GzipCompressorInputStream(inputStream))) {

                TarArchiveEntry entry;
                String rootDirName = rootDirectory.orElse(null);

                // links are created at the end, once everything else is unpacked
                List<Runnable> deferredSymlinks = new ArrayList<>();

                while ((entry = tar.getNextEntry()) != null) {
                    String entryName = entry.getName();

                    if (rootDirName != null) {
                        if (entryName.equals(rootDirName) ||
                            entryName.equals(rootDirName + "/")) {
                            // this is the root dir entry itself → nothing to extract
                            continue;
                        }
                        if (entryName.startsWith(rootDirName + "/")) {
                            entryName = entryName.substring(rootDirName.length() + 1);
                        }
                    }

                    Path target = extractRoot.resolve(entryName).normalize();

                    // defence-in-depth: prevent “../” escaping
                    if (!target.startsWith(extractRoot)) {
                        throw new IOException("Entry tries to escape target dir: " + entry.getName());
                    }

                    if (entry.isDirectory()) {
                        Files.createDirectories(target);
                        continue;
                    }


                    /* ---------- symbolic or hard link ---------- */
                    if (entry.isSymbolicLink() || entry.isLink()) {
                        String linkTarget = entry.getLinkName();           // may be relative
                        TarArchiveEntry finalEntry = entry;
                        Runnable task = () -> {
                            try {
                                Files.createDirectories(target.getParent());
                                if (finalEntry.isSymbolicLink()) {
                                    Files.deleteIfExists(target);
                                    Files.createSymbolicLink(
                                        target, Paths.get(linkTarget));
                                } else {                               // hard link
                                    Files.deleteIfExists(target);
                                    Files.createLink(
                                        target,
                                        destination.resolve(linkTarget).normalize());
                                }
                            } catch (IOException io) {
                                throw new UncheckedIOException(io);
                            }
                        };
                        deferredSymlinks.add(task);
                        continue;
                    }


                    // make sure parent dirs exist for files:
                    Files.createDirectories(target.getParent());

                    // 1 – copy bytes
                    Files.copy(tar, target, StandardCopyOption.REPLACE_EXISTING);

                    // 2 – translate mode → Set<PosixFilePermission>
                    if (Files.getFileStore(target)
                        .supportsFileAttributeView(PosixFileAttributeView.class)) {

                        int m = entry.getMode();          // e.g. 0755
                        Set<PosixFilePermission> perms = EnumSet.noneOf(PosixFilePermission.class);

                        if ((m & 0400) != 0) perms.add(PosixFilePermission.OWNER_READ);
                        if ((m & 0200) != 0) perms.add(PosixFilePermission.OWNER_WRITE);
                        if ((m & 0100) != 0) perms.add(PosixFilePermission.OWNER_EXECUTE);

                        if ((m & 0040) != 0) perms.add(PosixFilePermission.GROUP_READ);
                        if ((m & 0020) != 0) perms.add(PosixFilePermission.GROUP_WRITE);
                        if ((m & 0010) != 0) perms.add(PosixFilePermission.GROUP_EXECUTE);

                        if ((m & 0004) != 0) perms.add(PosixFilePermission.OTHERS_READ);
                        if ((m & 0002) != 0) perms.add(PosixFilePermission.OTHERS_WRITE);
                        if ((m & 0001) != 0) perms.add(PosixFilePermission.OTHERS_EXECUTE);

                        // 3 – apply
                        Files.setPosixFilePermissions(target, perms);
                    }
                }

                /* second pass so link targets are guaranteed to exist */
                for (Runnable r : deferredSymlinks) r.run();

            }
        }

        return destination;
    }

    @SneakyThrows
    private static Optional<String> findRootDirectory(File archive) {
        try (FileInputStream fileInputStream = new FileInputStream(archive)) {
            try (BufferedInputStream inputStream = new BufferedInputStream(fileInputStream);
                 TarArchiveInputStream tar = new TarArchiveInputStream(new GzipCompressorInputStream(inputStream))) {

                TarArchiveEntry entry;

                String commonRoot = null;

                while ((entry = tar.getNextEntry()) != null) {
                    String name = entry.getName();

                    // ignore "." or "./" prefixes that some tools insert
                    if (name.startsWith("./")) name = name.substring(2);

                    // skip empty names (possible in pax headers)
                    if (name.isEmpty()) continue;

                    String first = name.split("/", 2)[0];

                    if (commonRoot == null) {
                        commonRoot = first;
                    } else if (!commonRoot.equals(first)) {
                        commonRoot = null;
                        break;
                    }
                }

                return Optional.ofNullable(commonRoot);
            }
        }
    }
}
