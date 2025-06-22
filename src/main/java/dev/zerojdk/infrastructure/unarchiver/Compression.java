package dev.zerojdk.infrastructure.unarchiver;

import java.io.InputStream;

public interface Compression {
    InputStream decompress(InputStream in);
}
