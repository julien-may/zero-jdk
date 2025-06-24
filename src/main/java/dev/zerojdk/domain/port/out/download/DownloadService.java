package dev.zerojdk.domain.port.out.download;

import java.io.File;

public interface DownloadService {
    File download(String uri);

    default File download(String uri, ProgressListener listener) {
        return download(uri);
    }

    interface ProgressListener {
        void onProgress(long bytesRead, long totalBytes);
    }
}
