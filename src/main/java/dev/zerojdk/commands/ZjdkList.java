package dev.zerojdk.commands;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.catalog.CatalogRepository;
import dev.zerojdk.utils.OperatingSystem;
import dev.zerojdk.utils.ProcessorArchitecture;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import picocli.CommandLine;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

@CommandLine.Command(header = "List installed or available JDK releases")
public class ZjdkList {
    @CommandLine.Command(name = "installed", header = "Show installed JDKs")
    static class Installed implements Runnable {
        @Override
        public void run() {

        }
    }

    @RequiredArgsConstructor
    @CommandLine.Command(name = "available", header = "Show available JDKs")
    public static class Available implements Runnable {
        private final CatalogRepository catalogRepository;

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
                    catalogRepository.findLatest(operatingSystem, processorArchitecture);

                latestByDistro.keySet().stream().sorted(Comparator.naturalOrder())
                    .forEach(distribution -> {
                        System.out.printf("%s\n", distribution);

                        printVersions(latestByDistro.get(distribution), 2,
                            Comparator.comparingInt(lv -> JdkVersion.Support.LTS == lv.getSupport() ? 0 : 1));
                    });
            } else {
                List<JdkVersion> versions = all
                    ? catalogRepository.findAllByDistribution(operatingSystem, processorArchitecture, distribution).stream()
                        .sorted(Comparator.comparing(JdkVersion::getDistributionVersion))
                        .toList()
                    : catalogRepository.findLatestByDistribution(operatingSystem, processorArchitecture, distribution);

                Comparator<JdkVersion> comparator = all
                    ? Comparator.comparing(JdkVersion::getDistributionVersion).reversed()
                    : Comparator.comparingInt(lv -> JdkVersion.Support.LTS == lv.getSupport() ? 0 : 1);

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
    }
}
