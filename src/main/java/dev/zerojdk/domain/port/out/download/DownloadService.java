package dev.zerojdk.domain.port.out.download;

import java.io.File;

public interface DownloadService {
    File download(String uri);
}
