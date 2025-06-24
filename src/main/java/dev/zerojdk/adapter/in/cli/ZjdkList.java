package dev.zerojdk.adapter.in.cli;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.port.out.PlatformDetection;
import dev.zerojdk.domain.service.catalog.CatalogService;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CommandLine.Command(header = "List installed or available JDK releases")
public class ZjdkList {
    @CommandLine.Command(name = "installed", header = "Show installed JDKs")
    static class Installed implements Runnable {
        @Override
        public void run() {

        }
    }

    @RequiredArgsConstructor
    @CommandLine.Command(header = "Show available JDKs")
    public static class Available implements Runnable {
        private final PlatformDetection platformDetection;
        private final CatalogService catalogService;

        @CommandLine.Option(names = {"--dist"}, description = "The distribution")
        private String distribution;
        @CommandLine.Option(names = {"--all"}, description = "Shows all available version of a distribution")
        private boolean all;

        @Override
        public void run() {
            Platform platform = platformDetection.detect();

            if (distribution == null) {
                Map<String, List<JdkVersion>> latest = catalogService.findLatest(platform);

                latest.keySet().stream().sorted().forEach(dist -> {
                    System.out.println(dist);
                    printVersions(latest.get(dist), 2, groupBySupportThenSort());
                });
            } else {
                List<JdkVersion> versions = all
                    ? catalogService.findAllByDistribution(platform, distribution).stream()
                        .sorted(Comparator.comparing(JdkVersion::getDistributionVersion))
                        .toList()
                    : catalogService.findLatestByDistribution(platform, distribution);

                Comparator<JdkVersion> ordering = all
                    ? Comparator.comparing(JdkVersion::getDistributionVersion).reversed()
                    : this::compareLtsFirst;

                printVersions(versions, 0, ordering);
            }
        }

        private void printVersions(List<JdkVersion> versions, int indent, Comparator<JdkVersion> ordering) {
            String whitespace = " ".repeat(indent);

            mergeVariants(versions).stream()
                .sorted(ordering)
                .forEach(v -> printVersion(whitespace, v));
        }

        private List<JdkVersion> mergeVariants(List<JdkVersion> versions) {
            return versions.stream()
                .collect(Collectors.groupingBy(JdkVersion::getDistributionVersion))
                .values().stream()
                .map(this::mergeGroup)
                .toList();
        }

        private JdkVersion mergeGroup(List<JdkVersion> group) {
            JdkVersion first = group.getFirst();
            JdkVersion result = new JdkVersion();

            result.setDistribution(first.getDistribution());
            result.setDistributionVersion(first.getDistributionVersion());
            result.setJavaVersion(first.getJavaVersion());
            result.setMajorVersion(first.getMajorVersion());
            result.setPlatform(first.getPlatform());
            result.setSupport(first.getSupport());
            result.setLink(first.getLink());
            result.setIdentifier(group.stream()
                .map(JdkVersion::getIdentifier)
                .collect(Collectors.joining(" ")));

            return result;
        }

        private void printVersion(String ws, JdkVersion v) {
            System.out.printf("%sVersion:       %s (%d - %s)%n", ws, v.getDistributionVersion(), v.getMajorVersion(), v.getJavaVersion());
            System.out.printf("%sIdentifier(s): %s%n", ws, v.getIdentifier());
            System.out.printf("%sSupport:       %s%n", ws, v.getSupport() == JdkVersion.Support.LTS ? "LTS" : "Non-LTS");
            System.out.printf("%sLink:          %s%n", ws, v.getLink());
            System.out.println();
        }

        private int compareLtsFirst(JdkVersion a, JdkVersion b) {
            return (a.getSupport() == JdkVersion.Support.LTS ? 0 : 1)
                 - (b.getSupport() == JdkVersion.Support.LTS ? 0 : 1);
        }

        private Comparator<JdkVersion> groupBySupportThenSort() {
            return Comparator.comparingInt(v -> v.getSupport() == JdkVersion.Support.LTS ? 0 : 1);
        }
    }
}
