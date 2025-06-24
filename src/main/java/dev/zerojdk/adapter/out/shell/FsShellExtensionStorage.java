package dev.zerojdk.adapter.out.shell;

import dev.zerojdk.domain.port.out.shell.ShellExtensionLayout;
import dev.zerojdk.domain.port.out.shell.ShellExtensionStorage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@RequiredArgsConstructor
public class FsShellExtensionStorage implements ShellExtensionStorage {
    private final ShellExtensionLayout extensionLayout;

    @SneakyThrows
    @Override
    public Path write(String script) {
        return Files.writeString(
            extensionLayout.getZshPluginPath(),
            script,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        );
    }
}
