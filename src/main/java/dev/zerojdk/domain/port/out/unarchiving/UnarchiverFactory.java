package dev.zerojdk.domain.port.out.unarchiving;

import java.io.File;

public interface UnarchiverFactory {
    Unarchiver create(File archive);
}
