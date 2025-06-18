package dev.zerojdk.commands;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.zerojdk.ConfigurationNotFoundException;
import dev.zerojdk.UnsupportedIdentifierException;
import dev.zerojdk.utils.OperatingSystem;
import dev.zerojdk.utils.ProcessorArchitecture;
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

@CommandLine.Command(header = "Ensure that the JDK declared in the manifest is ready")
public class ZjdkSync implements Runnable {
    private static final File CATALOGUE = new File(System.getProperty("user.home"), ".zjdk/catalogue.json");

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .enable(SerializationFeature.INDENT_OUTPUT);

    @CommandLine.Option(names = {"--global"}, description = "Sync globally")
    private boolean global;

    private record JdkPaths(ZjdkList.JdkVersion version, Path extractedRoot, Path javaHome) {}

    @SneakyThrows
    @Override
    public void run() {
        // TODO: processing feedback
        //      System.out.printf("Downloading %s...", version.getIdentifier());

        sync(global);
    }

    public static void sync(boolean global) {
        ZjdkList.JdkVersion configuredJdkVersion = findConfiguredJdkVersion(
            findZjdkConfiguration(global
                ? ZjdkSync.SearchMode.USER_HOME
                : ZjdkSync.SearchMode.WORKSPACE));

        ensureRelease(configuredJdkVersion)
            .flatMap(root -> findJavaHome(root)
                .map(javaHome -> new JdkPaths(configuredJdkVersion, root, javaHome)))
        .ifPresent(paths -> registerJdk(
            paths.version(),
            paths.extractedRoot(),
            paths.javaHome()));
    }

    private static Optional<Path> ensureRelease(ZjdkList.JdkVersion version) {
        return findJdkRelease(version)
            .or(() -> downloadAndExtract(version));
    }

    private static Optional<Path> downloadAndExtract(ZjdkList.JdkVersion version) {
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

    private static Optional<Path> findJdkRelease(ZjdkList.JdkVersion jdkVersion) {
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
    private static void registerJdk(ZjdkList.JdkVersion jdkVersion, Path release, Path javaHome) {
        File info = new File(release.toFile(), ".info");

        Properties props = new Properties();
        props.setProperty("home", javaHome.toString());

        try (FileOutputStream fileOutputStream = new FileOutputStream(info)) {
            props.store(fileOutputStream, null);
        }
    }


    public enum SearchMode {
        /**
         * Check <code>~/.zjdk/config.properties</code> only.
         * */
        USER_HOME,
        /**
         * Start in the current working directory and walk upward,
         * <strong>skipping</strong> the user’s home directory if encountered,
         * then continue up to the filesystem root
         * */
        WORKSPACE,
        /**
         * Start in the current working directory and walk upward through
         * <strong>every</strong> ancestor directory, all the way to the
         * filesystem root.
         */
        FULL_TREE
    }

    /**
     * Locate the directory that contains a *zjdk* configuration.
     *
     * if {@code global == true}) checks only the user’s home directory for {@code ~/.zjdk/config.properties}; otherwise
     * starts in the current working directory and walks up the filesystem hierarchy, ignoring the user’s home directory
     * and stops before the filesystem root, returning the first ancestor that contains {@code .zjdk/config.properties}.
     *
     * @param global {@code true} to search exclusively in the user’s home
     *               directory; {@code false} to search upward from the current
     *               directory (excluding the home directory).
     * @return an {@link Optional} with the path of the directory that holds the
     *         zjdk configuration, or {@link Optional#empty()} if none is found
     */
//    public static Path findZjdkConfiguration(boolean global) {
//        Path home = Path.of(System.getProperty("user.home"));
//
//        Path path = global
//            ? home
//            : Path.of(".").toAbsolutePath();
//
//        do {
//            if (global || !path.equals(home)) {
//                Path config = path.resolve(".zjdk", "config.properties");
//
//                if (config.toFile().exists()) {
//                    return config;
//                }
//            }
//            path = path.getParent();
//        } while(!global && !path.equals(Path.of("/")));
//
//        throw new ConfigurationNotFoundException();
//    }

    /**
     * Locate the directory that contains a *zjdk* configuration file.
     *
     * <ul>
     *   <li>{@link SearchMode#USER_HOME} &nbsp;→&nbsp; check <code>~/.zjdk/config.properties</code> only.</li>
     *   <li>{@link SearchMode#WORKSPACE} &nbsp;→&nbsp; walk up from the current directory,
     *       <b>stopping before</b> the user’s home directory.</li>
     *   <li>{@link SearchMode#FULL_TREE} &nbsp;→&nbsp; walk up from the current directory
     *       <b>through</b> the user’s home directory and on to the filesystem root.</li>
     * </ul>
     *
     * @param mode search strategy (never {@code null})
     * @return a {@link Path} to the first matching <code>config.properties</code>
     * @throws ConfigurationNotFoundException if no configuration could be located
     */
    public static Path findZjdkConfiguration(SearchMode mode) {
        Objects.requireNonNull(mode, "mode");

        Path home = Path.of(System.getProperty("user.home")).toAbsolutePath();
        Path path = (mode == SearchMode.USER_HOME)
            ? home
            : Path.of(".").toAbsolutePath().normalize();

        while (path != null) {
            if (mode != SearchMode.WORKSPACE || !path.equals(home)) {
                Path candidate = path.resolve(".zjdk").resolve("config.properties");
                if (Files.exists(candidate)) {
                    return candidate;
                }
            }

            if (mode == SearchMode.USER_HOME || path.getParent() == null) {
                break;
            }

            path = path.getParent();
        }

        throw new ConfigurationNotFoundException();
    }


    @SneakyThrows
    public static String findVersionInConfig(Path configFile) {
        Properties properties = new Properties();
        properties.load(new FileReader(configFile.toFile()));

        return Optional.ofNullable(properties.getProperty("version"))
            .orElseThrow(ConfigurationNotFoundException::new);
    }

    private static ZjdkList.JdkVersion findConfiguredJdkVersion(Path root) {
        String identifier = findVersionInConfig(root);

        return findByIdentifier(detectOperatingSystem(), detectProcessorArchitecture(), identifier)
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



    @SneakyThrows
    public static Optional<ZjdkList.JdkVersion> findByIdentifier(OperatingSystem os, ProcessorArchitecture arch, String identifier) {
        List<ZjdkList.JdkVersion> catalogue = MAPPER.readValue(CATALOGUE, new TypeReference<>() {});

        return catalogue.stream()
            .filter(jdkVersion -> jdkVersion.getIdentifier().equals(identifier))
            .filter(jdkVersion -> jdkVersion.getOperatingSystem().equals(os))
            .filter(jdkVersion -> jdkVersion.getArchitecture().equals(arch))
            .findFirst();
    }
}
