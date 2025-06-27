package dev.zerojdk.adapter.out.catalog.storage.download;

import dev.zerojdk.adapter.out.github.client.model.Asset;
import dev.zerojdk.adapter.out.github.client.GitHubReleaseClient;
import dev.zerojdk.adapter.out.github.client.model.Release;
import dev.zerojdk.domain.model.catalog.Catalog;
import dev.zerojdk.domain.port.out.catalog.CatalogDownloadService;
import dev.zerojdk.domain.port.out.unarchiving.Unarchiver;
import dev.zerojdk.domain.port.out.unarchiving.UnarchiverFactory;
import dev.zerojdk.domain.service.catalog.CatalogUnchangedException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class HttpCatalogDownloadService implements CatalogDownloadService {
    private final GitHubReleaseClient gitHubReleaseClient;
    private final UnarchiverFactory unarchiverFactory;

    private static final String REPO = "julien-may/zero-jdk-catalog";

    @Override
    public Catalog downloadLatest() {
        Release latestRelease = gitHubReleaseClient.getLatestRelease(REPO);
        return download(latestRelease);
    }

    @Override
    public Catalog downloadLatestIfNewer(String currentVersion) {
        Release latestRelease = gitHubReleaseClient.getLatestRelease(REPO);
        String version = latestRelease.tagName();

        if (currentVersion != null && currentVersion.equals(version)) {
            throw new CatalogUnchangedException("Catalog is already up-to-date");
        }

        return download(latestRelease);
    }

    @SneakyThrows
    private Catalog download(Release latestRelease) {
        Asset asset = latestRelease.assets().stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Asset not found"));

        File downloaded = gitHubReleaseClient.downloadReleaseAsset(asset);
        Unarchiver unarchiver = unarchiverFactory.create(downloaded);

        Path tempExtractDir = Files.createTempDirectory("catalog-extract-");
        Path extracted = unarchiver.extract(tempExtractDir);

        if (!extracted.toFile().isFile()) {
            // TODO: proper exception
            throw new RuntimeException(String.format("Extracted file %s is not a file", extracted.toAbsolutePath()));
        }

        return new Catalog(latestRelease.tagName(), extracted);
    }
}