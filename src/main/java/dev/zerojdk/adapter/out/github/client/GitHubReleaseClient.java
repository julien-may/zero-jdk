package dev.zerojdk.adapter.out.github.client;

import dev.zerojdk.adapter.out.github.client.model.Asset;
import dev.zerojdk.adapter.out.github.client.model.Release;

import java.io.File;

public interface GitHubReleaseClient {
    Release getLatestRelease(String repo);
    File downloadReleaseAsset(Asset asset);
}
