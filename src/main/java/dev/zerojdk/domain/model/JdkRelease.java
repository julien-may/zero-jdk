package dev.zerojdk.domain.model;

import java.nio.file.Path;

public record JdkRelease(JdkVersion jdkVersion, Path installRoot, Path javaHome) { }
