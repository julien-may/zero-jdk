package dev.zerojdk.adapter.out.shell;

import dev.zerojdk.domain.port.out.BaseLayout;
import dev.zerojdk.domain.port.out.shell.ShellExtensionLayout;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class FsShellExtensionLayout implements ShellExtensionLayout {
    private final BaseLayout baseLayout;

    @Override
    public Path getZshPluginPath() {
        return ensureDirectoryExists().resolve("zjdk.plugin.zsh");
    }

    @SneakyThrows
    private Path ensureDirectoryExists() {
        return Files.createDirectories(baseLayout.baseDirectory(true)
            .resolve("extensions"));
    }
}
