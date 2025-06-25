package dev.zerojdk.infrastructure;

import picocli.CommandLine;

public class VersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() {
        String version = getClass().getPackage().getImplementationVersion();
        return new String[]{version != null ? version : "unknown"};
    }
}
