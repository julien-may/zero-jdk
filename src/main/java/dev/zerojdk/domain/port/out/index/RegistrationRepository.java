package dev.zerojdk.domain.port.out.index;

import dev.zerojdk.domain.model.InstallationRecord;

import java.util.Optional;

public interface RegistrationRepository {
    InstallationRecord register(InstallationRecord installationRecord);
    Optional<InstallationRecord> find(String identifier);
}
