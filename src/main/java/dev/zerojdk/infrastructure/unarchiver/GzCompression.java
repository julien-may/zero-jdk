package dev.zerojdk.infrastructure.unarchiver;

import lombok.SneakyThrows;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.InputStream;

public class GzCompression implements Compression{
    @SneakyThrows
    @Override
    public InputStream decompress(InputStream in) {
        return new GzipCompressorInputStream(in);
    }
}
