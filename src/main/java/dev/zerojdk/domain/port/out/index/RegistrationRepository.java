package dev.zerojdk.domain.port.out.index;

import dev.zerojdk.domain.model.InstallationRecord;

import java.util.Optional;

public interface RegistrationRepository {
    void register(InstallationRecord installationRecord);
    Optional<InstallationRecord> find(String identifier);
}
