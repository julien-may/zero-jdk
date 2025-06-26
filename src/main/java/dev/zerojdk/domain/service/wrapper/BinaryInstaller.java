package dev.zerojdk.domain.service.wrapper;

import dev.zerojdk.domain.port.out.wrapper.WrapperLayout;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.graalvm.nativeimage.ImageInfo;
import org.graalvm.nativeimage.ProcessProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RequiredArgsConstructor
public class BinaryInstaller {
    private final WrapperLayout wrapperLayout;

    @SneakyThrows
    public void install() {
        if (ImageInfo.inImageRuntimeCode()) {
            Path path = Path.of(ProcessProperties.getExecutableName());
            Files.copy(path, wrapperLayout.binaryPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
