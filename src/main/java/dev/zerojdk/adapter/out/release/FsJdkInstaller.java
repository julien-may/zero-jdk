package dev.zerojdk.adapter.out.release;

import dev.zerojdk.domain.model.InstallationRecord;
import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.index.RegistrationRepository;
import dev.zerojdk.domain.port.out.release.JdkInstaller;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class FsJdkInstaller implements JdkInstaller {
    private final RegistrationRepository repository;

    @Override
    public InstallationRecord install(JdkVersion version, Path extractedDir) {
        return findJavaHome(extractedDir)
            .map(javaHome ->
                repository.register(new InstallationRecord(version.getIdentifier(), extractedDir, javaHome)))
            .orElseThrow(() -> new IllegalStateException("Extracted JDK home does not exist or is invalid"));
    }

    @SneakyThrows
    private Optional<Path> findJavaHome(Path root) {
        try (Stream<Path> path = Files.walk(root)) {
            return path.filter(p -> p.getFileName().toString().equals("java"))
                .filter(Files::isExecutable)
                .findFirst()
                .map(Path::getParent)
                .map(Path::getParent);
        }
    }
}
