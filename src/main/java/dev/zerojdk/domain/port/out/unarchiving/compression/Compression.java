package dev.zerojdk.domain.port.out.unarchiving.compression;

import java.io.InputStream;

public interface Compression {
    InputStream decompress(InputStream in);
}
