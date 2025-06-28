package dev.zerojdk.adapter.out.download;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import org.junit.jupiter.api.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

class HttpDownloadServiceTest {
    private static byte[] binaryContent;
    private static WireMockServer wireMock;

    private final HttpDownloadService service = new HttpDownloadService();

    @BeforeAll
    static void loadContent() throws IOException {
        try (InputStream stream = HttpDownloadServiceTest.class.getResourceAsStream("/download/lorem.txt")) {
            binaryContent = new BufferedInputStream(stream).readAllBytes();
        }
    }

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(0); // random port
        wireMock.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @Test
    void downloadFetchesFileAndRenamesItAccordingToContentDisposition() throws Exception {
        // Given
        wireMock.stubFor(get(urlEqualTo("/foo"))
            .willReturn(buildResponse()
                .withHeader("Content-Disposition", "attachment; filename=\"file.txt\"")));

        // When
        File actual = service.download(buildUri("foo"));

        // Then
        assertThat(actual.getName())
            .isEqualTo("file.txt");
        assertThat(actual.toPath())
            .hasBinaryContent(binaryContent);
    }

    @Test
    void downloadFetchesFileAndRenamesItAccordingToPath() throws Exception {
        // Given
        wireMock.stubFor(get(urlEqualTo("/foo.txt"))
            .willReturn(buildResponse()));

        // When
        File actual = service.download(buildUri("foo.txt"));

        // Then
        assertThat(actual.getName())
            .isEqualTo("foo.txt");
        assertThat(actual.toPath())
            .hasBinaryContent(binaryContent);
    }

    @Test
    void downloadTriggersProgressListener() throws Exception {
        // Given
        wireMock.stubFor(get(urlEqualTo("/bar.txt"))
            .willReturn(buildResponse()));

        ProgressTracker tracker = new ProgressTracker();

        // When
        File actual = service.download(buildUri("bar.txt"), tracker);

        // Then
        assertThat(tracker.progresses)
            .containsExactly(
                new ProgressTracker.Progress(8192, binaryContent.length),
                new ProgressTracker.Progress(binaryContent.length, binaryContent.length));
        assertThat(actual).exists();
    }

    private ResponseDefinitionBuilder buildResponse() {
        return aResponse().withBody(binaryContent)
            .withHeader("Content-Length", String.valueOf(binaryContent.length));
    }

    private String buildUri(String fileName) {
        return wireMock.baseUrl() +  "/" + fileName;
    }

    static class ProgressTracker implements HttpDownloadService.ProgressListener {
        record Progress(long read, long total) {}

        final List<Progress> progresses = new ArrayList<>();

        @Override
        public void onProgress(long read, long total) {
            progresses.add(new Progress(read, total));
        }
    }
}
