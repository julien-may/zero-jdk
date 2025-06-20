package dev.zerojdk.commands;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import dev.zerojdk.utils.OperatingSystem;
import dev.zerojdk.utils.ProcessorArchitecture;
import lombok.Data;
import lombok.SneakyThrows;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

@CommandLine.Command(header = "List installed or available JDK releases", subcommands = ZjdkList.Available.class)
public class ZjdkList {
    @CommandLine.Command(name = "installed", header = "Show installed JDKs")
    static class Installed implements Runnable {
        @Override
        public void run() {

        }
    }

    @CommandLine.Command(name = "available", header = "Show available JDKs")
    static class Available implements Runnable {
        private static final File CATALOGUE = new File(System.getProperty("user.home"), ".zjdk/catalogue.json");
        private static final ObjectMapper MAPPER = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        @CommandLine.Option(names = {"--dist"}, description = "The distribution")
        private String distribution;
        @CommandLine.Option(names = {"--all"}, description = "Shows all available version of a distribution")
        private boolean all;

        @SneakyThrows
        @Override
        public void run() {
            OperatingSystem operatingSystem = OperatingSystem.detectOperatingSystem();
            ProcessorArchitecture processorArchitecture = ProcessorArchitecture.detectProcessorArchitecture();

            if (distribution == null) {
                Map<String, List<JdkVersion>> latestByDistro =
                    findLatestVersions(operatingSystem, processorArchitecture);


                latestByDistro.keySet().stream().sorted(Comparator.naturalOrder())
                    .forEach(distribution -> {
                        System.out.printf("%s\n", distribution);

                        printVersions(latestByDistro.get(distribution), 2,
                            Comparator.comparingInt(lv -> "LTS".equals(lv.getSupport()) ? 0 : 1));
                    });
            } else {
                List<JdkVersion> versions = all
                    ? findAllVersions(operatingSystem, processorArchitecture, distribution).stream()
                        .sorted(Comparator.comparing(JdkVersion::getDistributionVersion))
                        .toList()
                    : findLatestVersions(operatingSystem, processorArchitecture, distribution);

                Comparator<JdkVersion> comparator = all
                    ? Comparator.comparing(JdkVersion::getDistributionVersion).reversed()
                    : Comparator.comparingInt(lv -> "LTS".equals(lv.getSupport()) ? 0 : 1);

                printVersions(versions, 0, comparator);
            }
        }

        private void printVersions(List<JdkVersion> versions, int indent, Comparator<JdkVersion> comparator) {
            String whitespace = IntStream.range(0, indent).mapToObj(i -> " ").collect(Collectors.joining());

            // Merge bundled and non-bundled JavaFx versions of the same distribution versions together
            Stream<JdkVersion> latestVersionStream = versions.stream()
                .collect(groupingBy(JdkVersion::getDistributionVersion))
                .values().stream()
                .map(a -> {
                    JdkVersion first = a.getFirst();

                    JdkVersion latest = new JdkVersion();
                    latest.setDistribution(distribution);
                    latest.setDistributionVersion(first.getDistributionVersion());
                    latest.setMajorVersion(first.getMajorVersion());
                    latest.setJavaVersion(first.getJavaVersion());
                    latest.setIdentifier(a.stream()
                        .map(JdkVersion::getIdentifier)
                        .collect(Collectors.joining(" ")));
                    latest.setArchitecture(first.getArchitecture());
                    latest.setSupport(first.getSupport());
                    latest.setSupport(first.getSupport());
                    latest.setLink(first.getLink());

                    return latest;
                });

            latestVersionStream
                .sorted(comparator)
                .forEach(jdkVersion -> {
                    System.out.printf("%sVersion:       %s\n", whitespace, buildVersion(jdkVersion));
                    System.out.printf("%sIdentifier(s): %s\n", whitespace, jdkVersion.getIdentifier());
                    System.out.printf("%sSupport:       %s\n", whitespace, jdkVersion.getSupport());
                    System.out.printf("%sLink:          %s\n", whitespace, jdkVersion.getLink());
                    System.out.println();
                });
        }

        private String buildVersion(JdkVersion version) {
            return String.format("%s (%d - %s)",
                version.getDistributionVersion(),
                version.getMajorVersion(),
                version.getJavaVersion());
        }

        @SneakyThrows
        private Map<String, List<JdkVersion>> findAllVersions(OperatingSystem os, ProcessorArchitecture arch) {
            List<JdkVersion> catalogue = MAPPER.readValue(CATALOGUE, new TypeReference<>() {});

            return catalogue.stream()
                .filter(jdkVersion ->
                    jdkVersion.getOperatingSystem() == os && jdkVersion.getArchitecture() == arch)
                .collect(groupingBy(JdkVersion::getDistribution));
        }

        @SneakyThrows
        private Map<String, List<JdkVersion>> findLatestVersions(OperatingSystem os, ProcessorArchitecture arch) {
            List<JdkVersion> catalogue = MAPPER.readValue(CATALOGUE, new TypeReference<>() {});

            return catalogue.stream()
                .filter(jdkVersion ->
                    jdkVersion.getOperatingSystem() == os && jdkVersion.getArchitecture() == arch)
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

        private List<JdkVersion> findLatestVersions(OperatingSystem os, ProcessorArchitecture arch, String distribution) {
            return findLatestVersions(os, arch).getOrDefault(distribution, List.of());
        }

        private List<JdkVersion> findAllVersions(OperatingSystem os, ProcessorArchitecture arch, String distribution) {
            return findAllVersions(os, arch).getOrDefault(distribution, List.of());
        }
    }

    @Data
    public static class JdkVersion {
        private String distribution;
        @JsonDeserialize(using = RuntimeVersionDeserializer.class)
        private Runtime.Version distributionVersion;
        @JsonDeserialize(using = RuntimeVersionDeserializer.class)
        private Runtime.Version javaVersion;
        private int majorVersion;
        private boolean javafxBundled;
        private String identifier;
        private String support;
        private String link;
        @JsonDeserialize(using = OperatingSystemDeserializer.class)
        private OperatingSystem operatingSystem;
        @JsonDeserialize(using = ProcessorArchitectureDeserializer.class)
        private ProcessorArchitecture architecture;
        private String indirectDownloadUri;
        private String archiveType;
    }

    public static class OperatingSystemDeserializer extends StdDeserializer<OperatingSystem> {
        public OperatingSystemDeserializer() {
            super(String.class);
        }

        @Override
        public OperatingSystem deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            return switch (p.getText()) {
                case "linux" -> OperatingSystem.LINUX;
                case "windows" -> OperatingSystem.WINDOWS;
                case "macos" -> OperatingSystem.MACOS;
                case "aix" -> OperatingSystem.AIX;
                default -> null;
            };
        }
    }

    public static class ProcessorArchitectureDeserializer extends StdDeserializer<ProcessorArchitecture> {
        public ProcessorArchitectureDeserializer() {
            super(String.class);
        }

        @Override
        public ProcessorArchitecture deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            return switch (p.getText()) {
                case "aarch64" -> ProcessorArchitecture.AARCH64;
                default -> null;
            };
        }
    }

    public static class RuntimeVersionDeserializer extends StdDeserializer<Runtime.Version> {
        public RuntimeVersionDeserializer() {
            super(String.class);
        }

        @Override
        public Runtime.Version deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            return Runtime.Version.parse(p.getText());
        }
    }
}
