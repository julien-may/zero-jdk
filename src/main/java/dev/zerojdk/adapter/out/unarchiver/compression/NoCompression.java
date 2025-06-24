package dev.zerojdk.adapter.out.unarchiver.compression;

import dev.zerojdk.domain.port.out.unarchiving.compression.Compression;

import java.io.InputStream;

public class NoCompression implements Compression {
    @Override
    public InputStream decompress(InputStream in) {
        return in;
    }
}
