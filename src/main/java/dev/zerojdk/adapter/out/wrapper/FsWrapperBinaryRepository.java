package dev.zerojdk.adapter.out.wrapper;

import dev.zerojdk.domain.port.out.ProjectLayout;
import dev.zerojdk.domain.port.out.wrapper.WrapperBinaryRepository;
import dev.zerojdk.domain.service.ConfigurationNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Optional;

@RequiredArgsConstructor
public class FsWrapperBinaryRepository implements WrapperBinaryRepository {
    private final ProjectLayout projectLayout;

    @Override
    public boolean exists() {
        return binaryLocation()
            .map(Path::toFile)
            .map(File::exists)
            .orElse(false);
    }

    @SneakyThrows
    @Override
    public void save(InputStream in) {
        Path binaryPath = binaryPath(projectLayout.findProjectRoot(false)
            .orElseThrow(ConfigurationNotFoundException::new)); // TODO: Use different exception

        Files.createDirectories(binaryPath.getParent());
        Files.copy(in, binaryPath, StandardCopyOption.REPLACE_EXISTING);

        // Make it executable
        var perms = new HashSet<>(Files.getPosixFilePermissions(binaryPath));
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(binaryPath, perms);
    }

    @Override
    public Path executable() {
        return binaryLocation().orElseThrow();
    }

    private Optional<Path> binaryLocation() {
        return projectLayout.findProjectRoot(false)
            .map(this::binaryPath);
    }

    private Path binaryPath(Path parent) {
        return parent.resolve(".zjdk/wrapper/zjdk");
    }
}
