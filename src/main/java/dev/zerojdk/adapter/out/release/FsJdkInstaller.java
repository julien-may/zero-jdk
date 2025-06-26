package dev.zerojdk.adapter.out.release;

import dev.zerojdk.domain.model.release.InstallationRecord;
import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.release.JdkRegistrationRepository;
import dev.zerojdk.domain.port.out.release.JdkInstaller;
import dev.zerojdk.domain.port.out.release.JdkReleaseLayout;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class FsJdkInstaller implements JdkInstaller {
    private final JdkReleaseLayout jdkReleaseLayout;
    private final JdkRegistrationRepository repository;

    @SneakyThrows
    @Override
    public InstallationRecord install(JdkVersion version, Path jdk) {
        Path target = jdkReleaseLayout.ensureReleaseDirectory()
            .resolve(version.getIdentifier());

        if (!target.toFile().exists()) {
            Files.move(jdk, target, StandardCopyOption.ATOMIC_MOVE);
        }

        return findJavaHome(target)
            .map(javaHome ->
                repository.register(new InstallationRecord(version.getIdentifier(), target, javaHome)))
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
