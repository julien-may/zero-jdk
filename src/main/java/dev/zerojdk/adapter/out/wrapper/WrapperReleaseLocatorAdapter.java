package dev.zerojdk.adapter.out.wrapper;

import dev.zerojdk.adapter.out.github.client.GitHubReleaseClient;
import dev.zerojdk.adapter.out.github.client.model.Asset;
import dev.zerojdk.adapter.out.github.client.model.Release;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.port.out.wrapper.WrapperReleaseLocator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WrapperReleaseLocatorAdapter implements WrapperReleaseLocator {
    private static final String REPO = "julien-may/zero-jdk";

    private final GitHubReleaseClient gitHubReleaseClient;

    @Override
    public String findLatestUrl(Platform platform) {
        String os = switch (platform.os()) {
            case LINUX -> "linux";
            case MACOS -> "macos";
            case WINDOWS -> "windows";
            case AIX -> "aix";
        };

        String arch = switch (platform.arch()) {
            case AARCH64 -> "arm64";
            case X64 -> "x64";
        };

        return resolveGitHubLatest(os + "-" + arch);
    }

    private String resolveGitHubLatest(String platform) {
        Release latestRelease = gitHubReleaseClient.getLatestRelease(REPO);

        return latestRelease.assets().stream()
            .map(Asset::browserDownloadUrl)
            .filter(u -> u.contains(platform))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No asset for " + platform));
    }
}
