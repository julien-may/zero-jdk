package dev.zerojdk.adapter.out.wrapper;

import dev.zerojdk.domain.port.out.ProjectLayoutPort;
import dev.zerojdk.domain.port.out.wrapper.WrapperScriptStorePort;
import dev.zerojdk.domain.service.ConfigurationNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;

@RequiredArgsConstructor
public class FsWrapperScriptRepository implements WrapperScriptStorePort {
    private final ProjectLayoutPort projectLayoutPort;

    @SneakyThrows
    @Override
    public void save(String content) {
        Path wrapperPath = wrapperPath(projectLayoutPort.findProjectRoot(false)
            .orElseThrow(ConfigurationNotFoundException::new));// TODO: Use different exception

        Files.writeString(wrapperPath, content, StandardCharsets.UTF_8);

        // Make it executable
        var perms = new HashSet<>(Files.getPosixFilePermissions(wrapperPath));
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(wrapperPath, perms);
    }

    private Path wrapperPath(Path parent) {
        return parent.resolve("zjdkw");
    }
}
