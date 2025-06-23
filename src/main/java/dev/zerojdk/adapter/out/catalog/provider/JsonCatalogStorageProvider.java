package dev.zerojdk.adapter.out.catalog.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
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
import java.nio.file.StandardCopyOption;
import java.util.List;

@RequiredArgsConstructor
public class JsonCatalogStorageProvider implements CatalogStorageProvider {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    private final DownloadService downloadService;
    private final UnarchiverFactory unarchiverFactory;

    @SneakyThrows
    @Override
    public Path provide() {
        Path catalogPath = Path.of(System.getProperty("user.home"), ".zjdk/catalog.json");

        if (!Files.exists(catalogPath)) {
            File downloaded = downloadService.download(buildLatestUrl());
            Unarchiver unarchiver = unarchiverFactory.create(downloaded);
            Path extracted = unarchiver.extract(Path.of(System.getProperty("user.home"), ".zjdk"));

            Files.move(extracted, catalogPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return catalogPath;
    }

    @SneakyThrows
    public static String buildLatestUrl() {
        String repo = "julien-may/zero-jdk-catalog";

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/repos/" + repo + "/releases/latest"))
                .header("Accept", "application/vnd.github+json")
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to fetch release: " + response.statusCode());
            }

            Release release = MAPPER.readValue(response.body(), Release.class);

            return release.assets().stream()
                .map(Asset::browserDownloadUrl)
                .findFirst()
                .orElse(null);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Release(List<Asset> assets) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Asset(String browserDownloadUrl) {}

}
