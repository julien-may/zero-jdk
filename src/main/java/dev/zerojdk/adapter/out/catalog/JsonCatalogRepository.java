package dev.zerojdk.adapter.out.catalog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import dev.zerojdk.adapter.out.catalog.model.JsonJdkVersion;
import dev.zerojdk.adapter.out.catalog.provider.CatalogStorageProvider;
import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.port.out.catalog.CatalogRepository;
import dev.zerojdk.domain.model.OperatingSystem;
import dev.zerojdk.domain.model.ProcessorArchitecture;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
public class JsonCatalogRepository implements CatalogRepository {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    private final CatalogStorageProvider catalogStorageProvider;

    @Override
    public Map<String, List<JdkVersion>> findAll(Platform platform) {
        return readAll().stream()
            .filter(jdkVersion ->
                jdkVersion.getPlatform().equals(platform))
            .collect(groupingBy(JdkVersion::getDistribution));
    }

    @Override
    public List<JdkVersion> findAllByDistribution(Platform platform, String distribution) {
        return findAll(platform).getOrDefault(distribution, List.of());
    }

    @Override
    public Map<String, List<JdkVersion>> findLatest(Platform platform) {
        return readAll().stream()
            .filter(jdkVersion ->
                jdkVersion.getPlatform().equals(platform))
            .collect(groupingBy(JdkVersion::getDistribution,
                collectingAndThen(
                    groupingBy(JdkVersion::getSupport,
                        collectingAndThen(toList(), list -> {
                            Runtime.Version max = list.stream()
                                .map(JdkVersion::getDistributionVersion)
                                .max(Comparator.naturalOrder())
                                .orElseThrow();

                            // The same distribution version can be used for bundled javafx versions
                            return list.stream()
                                .filter(j -> j.getDistributionVersion().equals(max))
                                .toList();
                        })),
                    m -> m.values().stream()
                        .flatMap(List::stream)
                        .toList())));
    }

    @Override
    public List<JdkVersion> findLatestByDistribution(Platform platform, String distribution) {
        return findLatest(platform).getOrDefault(distribution, List.of());
    }

    @Override
    public Optional<JdkVersion> findByIdentifier(Platform platform, String identifier) {
        return readAll().stream()
            .filter(jdkVersion -> jdkVersion.getIdentifier().equals(identifier))
            .filter(jdkVersion -> jdkVersion.getPlatform().equals(platform))
            .findFirst();
    }

    @SneakyThrows
    private List<JdkVersion> readAll() {
        // TODO: handle exception when provided storage does not exist
        return MAPPER.readValue(catalogStorageProvider.provide().location().toFile(),
                new TypeReference<List<JsonJdkVersion>>() {}).stream()
            .map(this::map)
            .toList();
    }

    private JdkVersion map(JsonJdkVersion jsonJdkVersion) {
        JdkVersion jdkVersion = new JdkVersion();

        jdkVersion.setDistribution(jsonJdkVersion.getDistribution());
        jdkVersion.setDistributionVersion(Runtime.Version.parse(jsonJdkVersion.getDistributionVersion()));
        jdkVersion.setJavaVersion(Runtime.Version.parse(jsonJdkVersion.getJavaVersion()));
        jdkVersion.setMajorVersion(jsonJdkVersion.getMajorVersion());
        jdkVersion.setJavafxBundled(jsonJdkVersion.isJavafxBundled());
        jdkVersion.setIdentifier(jsonJdkVersion.getIdentifier());
        jdkVersion.setSupport(switch (jsonJdkVersion.getSupport()) {
            case "LTS" -> JdkVersion.Support.LTS;
            case "Non-LTS" -> JdkVersion.Support.NON_LTS;
            default -> null;
        });
        jdkVersion.setLink(jsonJdkVersion.getLink());
        jdkVersion.setPlatform(new Platform(
            switch (jsonJdkVersion.getOperatingSystem()) {
                case "linux" -> OperatingSystem.LINUX;
                case "windows" -> OperatingSystem.WINDOWS;
                case "macos" -> OperatingSystem.MACOS;
                case "aix" -> OperatingSystem.AIX; // TODO: remove
                default -> null;
            },
            switch (jsonJdkVersion.getArchitecture()) {
                case "aarch64" -> ProcessorArchitecture.AARCH64;
                case "x64" -> ProcessorArchitecture.X64;
                default -> null;
            }
        ));
        jdkVersion.setIndirectDownloadUri(jsonJdkVersion.getIndirectDownloadUri());
        jdkVersion.setArchiveType(jsonJdkVersion.getArchiveType());

        return jdkVersion;
    }
}
