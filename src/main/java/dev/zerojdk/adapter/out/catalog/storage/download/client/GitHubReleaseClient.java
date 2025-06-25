package dev.zerojdk.adapter.out.catalog.storage.download.client;

import java.io.File;

public interface GitHubReleaseClient {
    Release getLatestRelease(String repo);
    File downloadReleaseAsset(Asset asset);
}
