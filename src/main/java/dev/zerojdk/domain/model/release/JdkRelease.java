package dev.zerojdk.domain.model.release;

import dev.zerojdk.domain.model.JdkVersion;

import java.nio.file.Path;

public record JdkRelease(JdkVersion jdkVersion, Path installRoot, Path javaHome) { }
