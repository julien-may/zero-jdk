package dev.zerojdk.infrastructure.unarchiver;

import java.nio.file.Path;

public interface Unarchiver {
    Path extract(Path target);
}
