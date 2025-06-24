package dev.zerojdk.adapter.out.download;

import dev.zerojdk.domain.port.out.download.DownloadService;
import lombok.SneakyThrows;

import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class HttpDownloadService implements DownloadService {
    public File download(String uri) {
        return download(uri, null);
    }

    @SneakyThrows
    @Override
    public File download(String uri, ProgressListener progressListener) {
        HttpRequest httpRequest = HttpRequest
            .newBuilder(URI.create(uri))
            .build();

        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL);

        Path tmp = Files.createTempFile("dl-", ".part");

        try (HttpClient httpClient = httpClientBuilder.build()) {
            HttpResponse<InputStream> response = httpClient.send(
                httpRequest, HttpResponse.BodyHandlers.ofInputStream());

            long totalBytes = response.headers()
                .firstValueAsLong("Content-Length")
                .orElse(-1);

            String fileName = response.headers()
                .firstValue("Content-Disposition")
                .flatMap(this::parseFilename)
                .orElseGet(() -> Paths.get(response.uri().getPath())
                    .getFileName().toString());

            try (InputStream in = response.body();
                 OutputStream out = Files.newOutputStream(tmp, StandardOpenOption.WRITE)) {

                byte[] buffer = new byte[8192];
                long bytesRead = 0;
                int len;

                long percentage;
                long prevPercentage = -1;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    bytesRead += len;

                    percentage = bytesRead * 100 / totalBytes;

                    if (progressListener != null) {
                        // Throttle
                        if (prevPercentage == -1 || percentage > prevPercentage) {
                            progressListener.onProgress(bytesRead, totalBytes);
                        }
                    }

                    prevPercentage = percentage;
                }
            }

            Path target = tmp.getParent().resolve(fileName);
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);

            return target.toFile();
        }
    }

    private Optional<String> parseFilename(String disposition) {
        if (disposition == null) return Optional.empty();

        for (String part : disposition.split(";")) {
            part = part.trim();

            if (part.toLowerCase().startsWith("filename*=")) {
                String v = part.substring(10);
                int pos = v.indexOf("''");
                if (pos > 0) {
                    return Optional.of(
                        URLDecoder.decode(v.substring(pos + 2), StandardCharsets.UTF_8));
                }
            }

            if (part.toLowerCase().startsWith("filename=")) {
                String v = part.substring(9).trim();
                if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
                    v = v.substring(1, v.length() - 1);
                }
                return Optional.of(v);
            }
        }
        return Optional.empty();
    }
}
