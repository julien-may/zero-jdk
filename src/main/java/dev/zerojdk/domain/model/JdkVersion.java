package dev.zerojdk.domain.model;

import lombok.Data;

@Data
public class JdkVersion {
    private String distribution;
    private Runtime.Version distributionVersion;
    private Runtime.Version javaVersion;
    private int majorVersion;
    private boolean javafxBundled;
    private String identifier;
    private Support support;
    private String link;
    private Platform platform;
    private String indirectDownloadUri;
    private String archiveType;

    public enum Support {
        LTS, NON_LTS
    }
}
