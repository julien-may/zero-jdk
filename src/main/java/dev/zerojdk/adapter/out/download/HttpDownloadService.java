package dev.zerojdk.adapter.out.download;

import dev.zerojdk.domain.port.out.download.DownloadService;

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
    private static final int BUFFER_SIZE = 8192;

    @Override
    public File download(String uri) throws IOException, InterruptedException {
        return download(uri, null);
    }

    @Override
    public File download(String uri, ProgressListener progressListener) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(uri)).build();
        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL);

        Path tempFile = Files.createTempFile("dl-", ".part");

        try (HttpClient httpClient = httpClientBuilder.build()) {
            HttpResponse<InputStream> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofInputStream());

            long totalBytes = response.headers()
                .firstValueAsLong("Content-Length")
                .orElse(-1);

            String fileName = resolveFileName(response);

            writeToFile(response.body(), tempFile, totalBytes, progressListener);

            Path targetFile = tempFile.getParent().resolve(fileName);
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);

            return targetFile.toFile();
        } catch (InterruptedException | IOException e) {
            Files.deleteIfExists(tempFile);
            throw e;
        }
    }

    private void writeToFile(InputStream input, Path outputPath, long totalBytes, ProgressListener progressListener) throws IOException {
        try (InputStream in = input;
             OutputStream out = Files.newOutputStream(outputPath, StandardOpenOption.WRITE)) {

            byte[] buffer = new byte[BUFFER_SIZE];
            long bytesRead = 0;
            long lastReportedPercentage = -1;

            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                bytesRead += len;

                if (progressListener != null && totalBytes > 0) {
                    long percentage = bytesRead * 100 / totalBytes;
                    if (percentage != lastReportedPercentage) {
                        progressListener.onProgress(bytesRead, totalBytes);
                        lastReportedPercentage = percentage;
                    }
                }
            }
        }
    }

    private String resolveFileName(HttpResponse<?> response) {
        return response.headers()
            .firstValue("Content-Disposition")
            .flatMap(this::parseFilename)
            .orElseGet(() -> {
                Path path = Paths.get(response.uri().getPath());
                return path.getFileName().toString();
            });
    }

    private Optional<String> parseFilename(String disposition) {
        if (disposition == null) return Optional.empty();

        for (String part : disposition.split(";")) {
            String trimmed = part.trim();

            if (trimmed.toLowerCase().startsWith("filename*=")) {
                String value = trimmed.substring(10);
                int idx = value.indexOf("''");
                if (idx > 0) {
                    return Optional.of(URLDecoder.decode(value.substring(idx + 2), StandardCharsets.UTF_8));
                }
            }

            if (trimmed.toLowerCase().startsWith("filename=")) {
                String value = trimmed.substring(9).trim();
                if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                    value = value.substring(1, value.length() - 1);
                }
                return Optional.of(value);
            }
        }

        return Optional.empty();
    }
}
