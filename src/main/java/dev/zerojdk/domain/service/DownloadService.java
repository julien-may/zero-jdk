package dev.zerojdk.domain.service;

import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

public class DownloadService {
    @SneakyThrows
    public File download(String uri) {
        HttpRequest httpRequest = HttpRequest
            .newBuilder(URI.create(uri))
            .build();

        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL);

        Path tmp = Files.createTempFile("dl-", ".part");

        try (HttpClient httpClient = httpClientBuilder.build()) {
            HttpResponse<Path> response = httpClient.send(
                httpRequest,
                HttpResponse.BodyHandlers.ofFile(tmp, StandardOpenOption.WRITE));

            String fileName = response.headers()
                .firstValue("Content-Disposition")
                .flatMap(this::parseFilename)
                .orElseGet(() -> Paths.get(response.uri().getPath())
                    .getFileName().toString());

            Path target = tmp.getParent().resolve(fileName);

            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);

            return target.toFile();
        }
    }

    private Optional<String> parseFilename(String disposition) {
        if (disposition == null) return Optional.empty();

        for (String part : disposition.split(";")) {
            part = part.trim();

            if (part.toLowerCase().startsWith("filename*=")) {
                String v = part.substring(10);
                int pos = v.indexOf("''");
                if (pos > 0) {
                    return Optional.of(
                        URLDecoder.decode(v.substring(pos + 2), StandardCharsets.UTF_8));
                }
            }

            if (part.toLowerCase().startsWith("filename=")) {
                String v = part.substring(9).trim();
                if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
                    v = v.substring(1, v.length() - 1);
                }
                return Optional.of(v);
            }
        }
        return Optional.empty();
    }

    @SneakyThrows
    public Path extract(File archive, File destination) {
        Optional<String> rootDirectory = findRootDirectory(archive);
        Path extractRoot = destination.toPath();

        try (FileInputStream fileInputStream = new FileInputStream(archive);
             BufferedInputStream inputStream = new BufferedInputStream(fileInputStream);
             TarArchiveInputStream tar = new TarArchiveInputStream(new GzipCompressorInputStream(inputStream))) {

            TarArchiveEntry entry;
            String rootDirName = rootDirectory.orElse(null);
            List<Runnable> deferredSymlinks = new ArrayList<>();

            while ((entry = tar.getNextEntry()) != null) {
                String entryName = entry.getName();

                if (rootDirName != null) {
                    if (entryName.equals(rootDirName) ||
                        entryName.equals(rootDirName + "/")) {
                        continue;
                    }
                    if (entryName.startsWith(rootDirName + "/")) {
                        entryName = entryName.substring(rootDirName.length() + 1);
                    }
                }

                Path target = extractRoot.resolve(entryName).normalize();

                if (!target.startsWith(extractRoot)) {
                    throw new IOException("Entry tries to escape target dir: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                    continue;
                }

                if (entry.isSymbolicLink() || entry.isLink()) {
                    String linkTarget = entry.getLinkName();
                    TarArchiveEntry finalEntry = entry;
                    Runnable task = () -> {
                        try {
                            Files.createDirectories(target.getParent());
                            if (finalEntry.isSymbolicLink()) {
                                Files.deleteIfExists(target);
                                Files.createSymbolicLink(
                                    target, Paths.get(linkTarget));
                            } else {
                                Files.deleteIfExists(target);
                                Files.createLink(
                                    target,
                                    destination.toPath().resolve(linkTarget).normalize());
                            }
                        } catch (IOException io) {
                            throw new UncheckedIOException(io);
                        }
                    };
                    deferredSymlinks.add(task);
                    continue;
                }

                Files.createDirectories(target.getParent());
                Files.copy(tar, target, StandardCopyOption.REPLACE_EXISTING);

                if (Files.getFileStore(target)
                    .supportsFileAttributeView(PosixFileAttributeView.class)) {

                    int m = entry.getMode();
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

                    Files.setPosixFilePermissions(target, perms);
                }
            }

            for (Runnable r : deferredSymlinks) r.run();
        }

        return destination.toPath();
    }

    @SneakyThrows
    private Optional<String> findRootDirectory(File archive) {
        try (FileInputStream fileInputStream = new FileInputStream(archive);
             BufferedInputStream inputStream = new BufferedInputStream(fileInputStream);
             TarArchiveInputStream tar = new TarArchiveInputStream(new GzipCompressorInputStream(inputStream))) {

            TarArchiveEntry entry;
            String commonRoot = null;

            while ((entry = tar.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.startsWith("./")) name = name.substring(2);
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
