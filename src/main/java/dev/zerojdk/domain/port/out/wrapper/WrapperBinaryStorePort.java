package dev.zerojdk.domain.port.out.wrapper;

import java.io.InputStream;
import java.nio.file.Path;

public interface WrapperBinaryStorePort {
    boolean exists();
    void save(InputStream in);
    Path executable();
}
