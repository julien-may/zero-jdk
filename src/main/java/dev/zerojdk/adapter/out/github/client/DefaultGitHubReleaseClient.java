package dev.zerojdk.adapter.out.github.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import dev.zerojdk.adapter.out.github.client.model.Asset;
import dev.zerojdk.adapter.out.github.client.model.Release;
import dev.zerojdk.domain.port.out.download.DownloadService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

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

        HttpClient.Builder httpClientbuilder = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10));

        try (HttpClient client = httpClientbuilder.build()) {
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
