package dev.zerojdk.utils;

public enum ProcessorArchitecture {
    AARCH64;

    //""
    //"amd64"
    //"arm"
    //"arm64"
    //"ppc"
    //"ppc64"
    //"ppc64le"
    //"riscv64"
    //"s390x"
    //"sparcv9"
    //"x64"
    //"x86"


    public static ProcessorArchitecture detectProcessorArchitecture() {
        String architecture = System.getProperty("os.arch").toLowerCase();

        if (architecture.contains("aarch64")) {
            return AARCH64;
        }

        throw new UnsupportedOperationException("Processor architecture not supported: " + architecture);
    }
}
