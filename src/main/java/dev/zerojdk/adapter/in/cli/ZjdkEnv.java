package dev.zerojdk.adapter.in.cli;

import dev.zerojdk.adapter.out.UnmanagedDirectoryException;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.port.out.PlatformDetection;
import dev.zerojdk.domain.service.JdkConfigService;
import dev.zerojdk.domain.service.JdkReleaseService;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine;

@RequiredArgsConstructor
@CommandLine.Command(header = "Print environment variables for the active JDK")
public class ZjdkEnv implements Runnable {
    private final PlatformDetection platformDetection;
    private final JdkConfigService jdkConfigService;
    private final JdkReleaseService jdkReleaseService;

    @Override
    public void run() {
        Platform platform = platformDetection.detect();
        String version = getActiveVersion();

        jdkReleaseService.findJdkRelease(platform, version).ifPresent(release -> {
            System.out.printf("export JAVA_HOME=\"%s\"\n", release.javaHome());
            System.out.println("export PATH=\"$JAVA_HOME/bin:$PATH\"");
        });
    }

    private String getActiveVersion() {
        try {
            return jdkConfigService.getActiveVersion(false);
        } catch (UnmanagedDirectoryException e) {
            return jdkConfigService.getActiveVersion(true);
        }
    }
}
