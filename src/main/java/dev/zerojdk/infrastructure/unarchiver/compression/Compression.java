package dev.zerojdk.infrastructure.unarchiver.compression;

import java.io.InputStream;

public interface Compression {
    InputStream decompress(InputStream in);
}
