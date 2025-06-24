package dev.zerojdk.domain.port.out.wrapper;

import java.nio.file.Path;

public interface WrapperLayout {
    Path ensureWrapperDirectory();
    Path binaryPath();
    Path configPath();
    Path scriptPath();
}
