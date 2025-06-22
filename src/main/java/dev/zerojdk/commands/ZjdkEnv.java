package dev.zerojdk.commands;

import dev.zerojdk.ConfigurationNotFoundException;
import dev.zerojdk.domain.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import picocli.CommandLine;

import java.io.File;
import java.io.FileReader;
import java.util.Optional;
import java.util.Properties;

@RequiredArgsConstructor
@CommandLine.Command(header = "Print environment variables for the active JDK")
public class ZjdkEnv implements Runnable {
    private final ConfigService configService;

    private static final File ZJDK_FOLDER = new File(System.getProperty("user.home"), ".zjdk");
    private static final File RELEASES_FOLDER = new File(ZJDK_FOLDER, "releases");

    @Override
    public void run() {
        String version = configService.getConfiguredIdentifier(false)
            .or(() -> configService.getConfiguredIdentifier(true))
            .orElseThrow(ConfigurationNotFoundException::new);

        findIndexEntry(version).ifPresent(entry -> {
            System.out.printf("export JAVA_HOME=\"%s\"\n", entry.javaHome());
            System.out.println("export PATH=\"$JAVA_HOME/bin:$PATH\"");
        });
    }

    @SneakyThrows
    private Optional<ZjdkSync.IndexEntry> findIndexEntry(String identifier) {
        File release = new  File(RELEASES_FOLDER, identifier);
        File info = new  File(release, ".info");

        Properties properties = new Properties();
        properties.load(new FileReader(info));

        return Optional.ofNullable(properties.getProperty("home"))
            .map(home -> new ZjdkSync.IndexEntry(identifier, release.getAbsolutePath(), home));
    }
}
