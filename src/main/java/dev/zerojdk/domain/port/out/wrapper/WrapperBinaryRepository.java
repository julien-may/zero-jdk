package dev.zerojdk.domain.port.out.wrapper;

import java.io.InputStream;
import java.nio.file.Path;

public interface WrapperBinaryRepository {
    boolean exists();
    void save(InputStream in);
    Path executable();
}
