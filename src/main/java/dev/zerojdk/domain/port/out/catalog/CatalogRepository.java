package dev.zerojdk.domain.port.out.catalog;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.utils.OperatingSystem;
import dev.zerojdk.utils.ProcessorArchitecture;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CatalogRepository {
    Map<String, List<JdkVersion>> findAll(OperatingSystem os, ProcessorArchitecture arch);
    List<JdkVersion> findAllByDistribution(OperatingSystem os, ProcessorArchitecture arch, String distribution);

    Map<String, List<JdkVersion>> findLatest(OperatingSystem os, ProcessorArchitecture arch);
    List<JdkVersion> findLatestByDistribution(OperatingSystem os, ProcessorArchitecture arch, String distribution);

    Optional<JdkVersion> findByIdentifier(OperatingSystem os, ProcessorArchitecture arch, String identifier);
}
