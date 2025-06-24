package dev.zerojdk.adapter.out.release;

import dev.zerojdk.domain.port.out.ProjectLayout;
import dev.zerojdk.domain.port.out.release.JdkReleaseLayout;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

@RequiredArgsConstructor
public class FsJdkReleaseLayout implements JdkReleaseLayout {
    private final ProjectLayout projectLayout;

    @Override
    public Path getReleaseDirectory() {
        return projectLayout.findProjectRoot(true)
            .orElseThrow(() -> new IllegalStateException("Project root not found"))
            .resolve("releases");
    }
}
