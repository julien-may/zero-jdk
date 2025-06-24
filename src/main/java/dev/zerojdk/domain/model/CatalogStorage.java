package dev.zerojdk.domain.model;

import java.nio.file.Path;

public record CatalogStorage(String version, Path location) { }
