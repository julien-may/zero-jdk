package dev.zerojdk.adapter.in.cli;

import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.service.WrapperInstaller;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine;

@RequiredArgsConstructor
@CommandLine.Command(header = "Create a wrapper script that auto-installs zjdk when needed")
public class ZjdkWrapper implements Runnable {
    private final WrapperInstaller wrapperInstaller;

    @Override
    public void run() {
        wrapperInstaller.install(Platform.detect());
    }
}
