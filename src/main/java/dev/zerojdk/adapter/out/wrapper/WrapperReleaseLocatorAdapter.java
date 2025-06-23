package dev.zerojdk.adapter.out.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.port.out.wrapper.WrapperReleaseLocatorPort;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@RequiredArgsConstructor
public class WrapperReleaseLocatorAdapter implements WrapperReleaseLocatorPort {
    private static final String REPO = "julien-may/zero-jdk";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

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
        };

        return resolveGitHubLatest(os + "-" + arch);
    }

    @SneakyThrows
    private static String resolveGitHubLatest(String platform) {
        HttpClient.Builder httpClientbuilder = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10));

        try (HttpClient client = httpClientbuilder.build()) {
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.github.com/repos/" + REPO + "/releases/latest"))
                .header("Accept", "application/vnd.github+json")
                .build();

            var res = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() != 200) {
                throw new IOException("GitHub API status " + res.statusCode());
            }

            Release release = OBJECT_MAPPER.readValue(res.body(), Release.class);

            return release.assets().stream()
                .map(Asset::browserDownloadUrl)
                .filter(u -> u.contains(platform))
                .findFirst()
                .orElseThrow(() -> new IOException("No asset for " + platform));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("GitHub request interrupted", e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Release(List<Asset> assets) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Asset(String browserDownloadUrl) {}
}
