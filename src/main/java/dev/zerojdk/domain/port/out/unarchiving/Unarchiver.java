package dev.zerojdk.domain.port.out.unarchiving;

import java.nio.file.Path;

public interface Unarchiver {
    Path extract(Path target);
}
