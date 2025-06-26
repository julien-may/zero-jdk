package dev.zerojdk.domain.port.out.download;

import java.io.File;
import java.io.IOException;

public interface DownloadService {
    File download(String uri) throws IOException, InterruptedException;

    default File download(String uri, ProgressListener listener) throws IOException, InterruptedException {
        return download(uri);
    }

    interface ProgressListener {
        void onProgress(long bytesRead, long totalBytes);
    }
}
