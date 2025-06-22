package dev.zerojdk.commands;

import dev.zerojdk.ConfigurationNotFoundException;
import dev.zerojdk.UnsupportedIdentifierException;
import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.catalog.CatalogRepository;
import dev.zerojdk.domain.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import picocli.CommandLine;

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
import java.util.stream.Stream;

import static dev.zerojdk.utils.OperatingSystem.*;
import static dev.zerojdk.utils.ProcessorArchitecture.*;

@RequiredArgsConstructor
@CommandLine.Command(header = "Ensure that the JDK declared in the manifest is ready")
public class ZjdkSync implements Runnable {
    private final CatalogRepository catalogRepository;
    private final ConfigService configService;

    @CommandLine.Option(names = {"--global"}, description = "Sync globally")
    private boolean global;

    private record JdkPaths(JdkVersion version, Path extractedRoot, Path javaHome) {}

    @SneakyThrows
    @Override
    public void run() {
        // TODO: processing feedback
        //      System.out.printf("Downloading %s...", version.getIdentifier());

        sync(global);
    }

    public void sync(boolean global) {
        String identifier = configService.getConfiguredIdentifier(global)
            .orElseThrow(ConfigurationNotFoundException::new);

        JdkVersion configuredJdkVersion = findConfiguredJdkVersion(identifier);

        ensureRelease(configuredJdkVersion)
            .flatMap(root -> findJavaHome(root)
                .map(javaHome -> new JdkPaths(configuredJdkVersion, root, javaHome)))
        .ifPresent(paths -> registerJdk(
            paths.version(),
            paths.extractedRoot(),
            paths.javaHome()));
    }

    private static Optional<Path> ensureRelease(JdkVersion version) {
        return findJdkRelease(version)
            .or(() -> downloadAndExtract(version));
    }

    private static Optional<Path> downloadAndExtract(JdkVersion version) {
        Path targetParent = Path.of(System.getProperty("user.home"), ".zjdk");
        Path releases = targetParent.resolve("releases");

        try {
            Files.createDirectories(releases);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        Path archive = download(version.getIndirectDownloadUri()).toPath();
        Path extracted = extract(archive.toFile(), releases.resolve(version.getIdentifier()).toFile());

        return Optional.of(extracted);
    }

    private static Optional<Path> findJdkRelease(JdkVersion jdkVersion) {
        File zjdkHome = new File(System.getProperty("user.home"), ".zjdk");
        File releases = new File(zjdkHome, "releases");
        File jdkRelease = new File(releases, jdkVersion.getIdentifier());

        if (jdkRelease.exists()) {
            return Optional.of(jdkRelease.toPath());
        }

        return Optional.empty();
    }


    public record IndexEntry(String identifier, String release, String javaHome) { }


    @SneakyThrows
    private static void registerJdk(JdkVersion jdkVersion, Path release, Path javaHome) {
        File info = new File(release.toFile(), ".info");

        Properties props = new Properties();
        props.setProperty("home", javaHome.toString());

        try (FileOutputStream fileOutputStream = new FileOutputStream(info)) {
            props.store(fileOutputStream, null);
        }
    }

    private JdkVersion findConfiguredJdkVersion(String identifier) {
        return catalogRepository
            .findByIdentifier(detectOperatingSystem(), detectProcessorArchitecture(), identifier)
            .orElseThrow(() -> new UnsupportedIdentifierException(identifier));
    }

    @SneakyThrows
    private static File download(String uri) {
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
                .flatMap(ZjdkSync::parseFilename)
                .orElseGet(() -> Paths.get(response.uri().getPath())
                    .getFileName().toString());

            Path target = tmp.getParent().resolve(fileName);

            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);

            return target.toFile();
        }
    }

    private static Optional<String> parseFilename(String disposition) {
        if (disposition == null) return Optional.empty();

        for (String part : disposition.split(";")) {
            part = part.trim();

            // RFC 5987 / RFC 6266 - UTF-8:  filename*=utf-8''name%20with%20spaces.tar.gz
            if (part.toLowerCase().startsWith("filename*=")) {
                String v = part.substring(10);            // after "filename*="
                int pos = v.indexOf("''");
                if (pos > 0) {
                    return Optional.of(
                        URLDecoder.decode(v.substring(pos + 2), StandardCharsets.UTF_8));
                }
            }

            // Legacy ASCII:  filename="name.tar.gz"
            if (part.toLowerCase().startsWith("filename=")) {
                String v = part.substring(9).trim();
                if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
                    v = v.substring(1, v.length() - 1);   // strip quotes
                }
                return Optional.of(v);
            }
        }
        return Optional.empty();
    }


    @SneakyThrows
    private static Optional<Path> findJavaHome(Path root) {
        try (Stream<Path> path = Files.walk(root)) {
            return path.filter(p -> p.getFileName().toString().equals("java"))
                .filter(Files::isExecutable)
                .findFirst()
                .map(Path::getParent)
                .map(Path::getParent);
        }
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

    @SneakyThrows
    private static Path extract(File archive, File destination) {
        // if all entries share a single top-level dir this returns it, else empty
        Optional<String> rootDirectory = findRootDirectory(archive);

        // we always want to extract into  destination/alternativeName
        Path extractRoot = destination.toPath();//.resolve(alternativeName);

        try (FileInputStream fileInputStream = new FileInputStream(archive)) {
            try (BufferedInputStream inputStream = new BufferedInputStream(fileInputStream);
                 TarArchiveInputStream tar = new TarArchiveInputStream(new GzipCompressorInputStream(inputStream))) {

                TarArchiveEntry entry;
                String rootDirName = rootDirectory.orElse(null);

                // links are created at the end, once everything else is unpacked
                List<Runnable> deferredSymlinks = new ArrayList<>();

                while ((entry = tar.getNextEntry()) != null) {
                    String entryName = entry.getName();

                    /* -----------------------------------------------------------------
                     * 1. Strip the root-directory segment when it exists.
                     *    - skip the directory entry “foo/” itself
                     * ----------------------------------------------------------------- */
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
                                        destination.toPath().resolve(linkTarget).normalize());
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

        return destination.toPath();
    }
}
