package dev.zerojdk.adapter.in.cli;

import dev.zerojdk.adapter.in.cli.renderer.JdkVersionRenderer;
import dev.zerojdk.adapter.out.UnmanagedDirectoryException;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.port.out.PlatformDetection;
import dev.zerojdk.domain.service.catalog.CatalogService;
import dev.zerojdk.domain.service.config.JdkConfigService;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine;

@RequiredArgsConstructor
@CommandLine.Command(header = "Print information about the currently active JDK")
public class ZjdkInfo implements Runnable {
    private final PlatformDetection platformDetection;
    private final JdkConfigService jdkConfigService;
    private final CatalogService catalogService;

    @Override
    public void run() {
        Platform platform = platformDetection.detect();
        String version = getActiveVersion();

        JdkVersionRenderer jdkVersionRenderer = new JdkVersionRenderer();

        catalogService.findByIdentifier(platform, version)
            .ifPresent(release -> jdkVersionRenderer.render("", release));
    }

    private String getActiveVersion() {
        try {
            return jdkConfigService.getActiveVersion(false);
        } catch (UnmanagedDirectoryException e) {
            return jdkConfigService.getActiveVersion(true);
        }
    }
}
