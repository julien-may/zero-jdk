package dev.zerojdk.infrastructure.unarchiver.compression;

import lombok.SneakyThrows;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.InputStream;

public class GzipCompression implements Compression{
    @SneakyThrows
    @Override
    public InputStream decompress(InputStream in) {
        return new GzipCompressorInputStream(in);
    }
}
