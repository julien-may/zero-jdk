package dev.zerojdk.adapter.out.catalog.storage.download.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import dev.zerojdk.domain.port.out.download.DownloadService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RequiredArgsConstructor
public class DefaultGitHubReleaseClient implements GitHubReleaseClient {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    private final DownloadService  downloadService;

    @SneakyThrows
    @Override
    public Release getLatestRelease(String repo) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/repos/" + repo + "/releases/latest"))
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

    @Override
    public File downloadReleaseAsset(Asset asset) {
        return downloadService.download(asset.browserDownloadUrl());
    }
}
