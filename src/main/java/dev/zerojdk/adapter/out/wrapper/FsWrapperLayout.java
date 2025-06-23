package dev.zerojdk.adapter.out.wrapper;

import dev.zerojdk.domain.port.out.ProjectLayout;
import dev.zerojdk.domain.port.out.wrapper.WrapperLayout;
import dev.zerojdk.domain.service.ConfigurationNotFoundException;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

@RequiredArgsConstructor
public class FsWrapperLayout implements WrapperLayout {
    private final ProjectLayout projectLayout;

    @Override
    public Path wrapperDirectory() {
        return projectLayout.findProjectRoot(false)
            .orElseThrow(ConfigurationNotFoundException::new)
            .resolve(".zjdk/wrapper");
    }

    @Override
    public Path binaryPath() {
        return wrapperDirectory()
            .resolve("zjdk");
    }

    @Override
    public Path configPath() {
        return wrapperDirectory()
            .resolve("zjdk-wrapper.properties");
    }

    @Override
    public Path scriptPath() {
        return projectLayout.findProjectRoot(false)
            .orElseThrow(ConfigurationNotFoundException::new)
            .resolve("zjdkw");
    }
}
