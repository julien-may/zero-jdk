package dev.zerojdk.adapter.out.release;

import dev.zerojdk.domain.port.out.BaseLayout;
import dev.zerojdk.domain.port.out.release.JdkReleaseLayout;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class FsJdkReleaseLayout implements JdkReleaseLayout {
    private final BaseLayout baseLayout;

    @SneakyThrows
    @Override
    public Path ensureReleaseDirectory() {
        return Files.createDirectories(
            baseLayout.baseDirectory(true)
                .resolve("releases"));
    }
}
