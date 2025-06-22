package dev.zerojdk.adapter.in.cli;

import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.service.ManifestSyncService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import picocli.CommandLine;

@RequiredArgsConstructor
@CommandLine.Command(header = "Ensure that the JDK declared in the manifest is ready")
public class ZjdkSync implements Runnable {
    private final ManifestSyncService manifestSyncService;

    @CommandLine.Option(names = {"--global"}, description = "Sync globally")
    private boolean global;

    @SneakyThrows
    @Override
    public void run() {
        Platform platform = Platform.detect();
        manifestSyncService.sync(platform, global);
    }
}
