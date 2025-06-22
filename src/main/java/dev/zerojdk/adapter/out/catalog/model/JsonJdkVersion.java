package dev.zerojdk.adapter.out.catalog.model;

import lombok.Data;

@Data
public class JsonJdkVersion {
    private String distribution;
    private String distributionVersion;
    private String javaVersion;
    private int majorVersion;
    private boolean javafxBundled;
    private String identifier;
    private String support;
    private String link;
    private String operatingSystem;
    private String architecture;
    private String indirectDownloadUri;
    private String archiveType;
}
