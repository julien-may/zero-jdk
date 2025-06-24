package dev.zerojdk.adapter.out.unarchiver.compression;

import dev.zerojdk.domain.port.out.unarchiving.compression.Compression;
import lombok.SneakyThrows;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.InputStream;

public class GzipCompression implements Compression {
    @SneakyThrows
    @Override
    public InputStream decompress(InputStream in) {
        return new GzipCompressorInputStream(in);
    }
}
