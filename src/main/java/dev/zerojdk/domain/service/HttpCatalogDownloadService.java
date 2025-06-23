package dev.zerojdk.domain.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import dev.zerojdk.domain.model.Catalog;
import dev.zerojdk.domain.port.out.download.DownloadService;
import dev.zerojdk.infrastructure.unarchiver.Unarchiver;
import dev.zerojdk.infrastructure.unarchiver.UnarchiverFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RequiredArgsConstructor
public class HttpCatalogDownloadService implements CatalogDownloadService {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    private final DownloadService downloadService;
    private final UnarchiverFactory unarchiverFactory;

    private static final String REPO = "julien-may/zero-jdk-catalog";

    @SneakyThrows
    @Override
    public Catalog downloadLatestIfNewer(String currentVersion) {
        Release latestRelease = fetchLatestReleaseInfo();
        String version = latestRelease.tagName();

        if (currentVersion != null && currentVersion.equals(version)) {
            throw new CatalogUnchangedException("Catalog is already up-to-date");
        }

        String url = latestRelease.assets().stream()
            .map(Asset::browserDownloadUrl)
            .findFirst()
            .orElse(null);


        File downloaded = downloadService.download(url);
        Unarchiver unarchiver = unarchiverFactory.create(downloaded);

        Path tempExtractDir = Files.createTempDirectory("catalog-extract-");
        Path extracted = unarchiver.extract(tempExtractDir);

        return new Catalog(version, extracted);
    }

    @SneakyThrows
    private Release fetchLatestReleaseInfo() {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/repos/" + REPO + "/releases/latest"))
            .header("Accept", "application/vnd.github+json")
            .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to fetch GitHub release, status code: " + response.statusCode());
            }

            return MAPPER.readValue(response.body(), Release.class);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Release(String tagName, List<Asset> assets) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Asset(String browserDownloadUrl) {}
}