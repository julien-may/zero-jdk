package dev.zerojdk.domain.port.out.catalog;

import java.nio.file.Path;

public record CatalogMetadata(String version, Path location) { }
