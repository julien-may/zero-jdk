package dev.zerojdk.adapter.in.cli;

import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.service.WrapperService;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine;

@RequiredArgsConstructor
@CommandLine.Command(header = "Create a wrapper script that auto-installs zjdk when needed")
public class ZjdkWrapper implements Runnable {
    private final WrapperService wrapperService;

    @Override
    public void run() {
        wrapperService.install(Platform.detect());
    }
}
