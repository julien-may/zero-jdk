package dev.zerojdk.infrastructure.unarchiver.compression;

import java.io.InputStream;

public class NoCompression implements Compression {
    @Override
    public InputStream decompress(InputStream in) {
        return in;
    }
}
