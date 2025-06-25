package dev.zerojdk.domain.model.release;

import java.nio.file.Path;

public record InstallationRecord(String identifier, Path installRoot, Path javaHome) { }
