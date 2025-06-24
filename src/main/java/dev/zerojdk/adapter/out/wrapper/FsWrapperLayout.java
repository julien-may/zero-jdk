package dev.zerojdk.adapter.out.wrapper;

import dev.zerojdk.domain.port.out.BaseLayout;
import dev.zerojdk.domain.port.out.wrapper.WrapperLayout;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class FsWrapperLayout implements WrapperLayout {
    private final BaseLayout baseLayout;

    @SneakyThrows
    @Override
    public Path ensureWrapperDirectory() {
        return Files.createDirectories(baseLayout.baseDirectory(false)
            .resolve("wrapper"));
    }

    @Override
    public Path binaryPath() {
        return ensureWrapperDirectory()
            .resolve("zjdk");
    }

    @Override
    public Path configPath() {
        return ensureWrapperDirectory()
            .resolve("zjdk-wrapper.properties");
    }

    @Override
    public Path scriptPath() {
        return baseLayout.discoverProjectRoot()
            // TODO: concrete exception
            .orElseThrow(RuntimeException::new)
            .resolve("zjdkw");
    }
}
