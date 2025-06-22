package dev.zerojdk.infrastructure.unarchiver;

import java.io.File;

public class UnarchiverFactory {
    public Unarchiver create(File archive) {
        String name = archive.getName().toLowerCase();
        if (name.endsWith(".tar.gz") || name.endsWith(".tgz")) {
            return new TarUnarchiver(archive.toPath(), new GzCompression());
        }
        if (name.endsWith(".tar")) {
            return new TarUnarchiver(archive.toPath(), new NoCompression());
        }
        throw new UnsupportedArchiveException("Unsupported archive: " + name);
    }
}
