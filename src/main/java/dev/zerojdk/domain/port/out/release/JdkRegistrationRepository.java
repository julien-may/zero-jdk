package dev.zerojdk.domain.port.out.release;

import dev.zerojdk.domain.model.release.InstallationRecord;

import java.util.Optional;

public interface JdkRegistrationRepository {
    InstallationRecord register(InstallationRecord installationRecord);
    Optional<InstallationRecord> find(String identifier);
}
