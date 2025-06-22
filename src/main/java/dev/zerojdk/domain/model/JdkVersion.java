package dev.zerojdk.domain.model;

import dev.zerojdk.utils.OperatingSystem;
import dev.zerojdk.utils.ProcessorArchitecture;
import lombok.Data;

@Data
public class JdkVersion {
    private String distribution;
    private Runtime.Version distributionVersion;
    private Runtime.Version javaVersion;
    private int majorVersion;
    private boolean javafxBundled;
    private String identifier;
    private String support;
    private String link;
    private OperatingSystem operatingSystem;
    private ProcessorArchitecture architecture;
    private String indirectDownloadUri;
    private String archiveType;
}
