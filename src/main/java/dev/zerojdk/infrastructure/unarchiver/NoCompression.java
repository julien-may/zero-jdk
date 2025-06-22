package dev.zerojdk.infrastructure.unarchiver;

import java.io.InputStream;

public class NoCompression implements Compression{
    @Override
    public InputStream decompress(InputStream in) {
        return in;
    }
}
