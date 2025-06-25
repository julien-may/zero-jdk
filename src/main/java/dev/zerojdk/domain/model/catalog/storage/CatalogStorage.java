package dev.zerojdk.domain.model.catalog.storage;

import java.nio.file.Path;

public record CatalogStorage(String version, Path location) { }
