package dev.zerojdk.domain.port.out.release;

import dev.zerojdk.domain.model.release.InstallationRecord;
import dev.zerojdk.domain.model.JdkVersion;

import java.nio.file.Path;

public interface JdkInstaller {
    InstallationRecord install(JdkVersion version, Path extractedDir);
}
