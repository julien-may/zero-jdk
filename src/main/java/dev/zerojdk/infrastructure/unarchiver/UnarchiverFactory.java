package dev.zerojdk.infrastructure.unarchiver;

import dev.zerojdk.infrastructure.unarchiver.compression.GzipCompression;
import dev.zerojdk.infrastructure.unarchiver.compression.NoCompression;

import java.io.File;

public class UnarchiverFactory {
    public Unarchiver create(File archive) {
        String name = archive.getName().toLowerCase();
        if (name.endsWith(".tar.gz") || name.endsWith(".tgz")) {
            return new TarUnarchiver(archive.toPath(), new GzipCompression());
        }

        if (name.endsWith(".tar")) {
            return new TarUnarchiver(archive.toPath(), new NoCompression());
        }

        if (name.endsWith(".gz")) {
            return new GzipUnarchiver(archive.toPath());
        }

        throw new UnsupportedArchiveException("Unsupported archive: " + name);
    }
}
