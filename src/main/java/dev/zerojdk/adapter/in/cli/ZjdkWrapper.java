package dev.zerojdk.adapter.in.cli;

import picocli.CommandLine;

@CommandLine.Command(header = "Create a wrapper script that auto-installs zjdk when needed")
public class ZjdkWrapper {
    // Installs to current directory where .zjdk is located. NEVER to the global directory!
    // The script - called zjdkw - manages information in the .zjdk/wrapper/zjdk-wrapper.properties and installs the binary into .zjdk/wrapper directory as well
}
