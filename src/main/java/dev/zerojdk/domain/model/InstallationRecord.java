package dev.zerojdk.domain.model;

import java.nio.file.Path;

public record InstallationRecord(String identifier, Path installRoot, Path javaHome) { }
