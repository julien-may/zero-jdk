package dev.zerojdk.adapter.in.cli;

import dev.zerojdk.domain.port.out.PlatformDetection;
import dev.zerojdk.domain.service.wrapper.WrapperInstaller;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine;

@RequiredArgsConstructor
@CommandLine.Command(header = "Create a wrapper script that auto-installs zjdk when needed")
public class ZjdkWrapper implements Runnable {
    private final PlatformDetection platformDetection;
    private final WrapperInstaller wrapperInstaller;

    @Override
    public void run() {
        wrapperInstaller.install(platformDetection.detect());
    }
}
