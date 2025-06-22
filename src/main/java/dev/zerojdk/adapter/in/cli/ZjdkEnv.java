package dev.zerojdk.adapter.in.cli;

import dev.zerojdk.ConfigurationNotFoundException;
import dev.zerojdk.domain.service.ConfigService;
import dev.zerojdk.domain.service.JdkReleaseService;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine;

@RequiredArgsConstructor
@CommandLine.Command(header = "Print environment variables for the active JDK")
public class ZjdkEnv implements Runnable {
    private final ConfigService configService;
    private final JdkReleaseService jdkReleaseService;

    @Override
    public void run() {
        String version = configService.getActiveVersion(false)
            .or(() -> configService.getActiveVersion(true))
            .orElseThrow(ConfigurationNotFoundException::new);

        jdkReleaseService.findJdkRelease(version).ifPresent(javaHome -> {
            System.out.printf("export JAVA_HOME=\"%s\"\n", javaHome);
            System.out.println("export PATH=\"$JAVA_HOME/bin:$PATH\"");
        });
    }
}
