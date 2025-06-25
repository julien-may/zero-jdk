package dev.zerojdk.adapter.out.unarchiver;

import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

public class PosixPermissions {
    private static final PosixFilePermission[] POSIX_PERMISSIONS = {
        PosixFilePermission.OTHERS_EXECUTE, // 0001
        PosixFilePermission.OTHERS_WRITE,   // 0002
        PosixFilePermission.OTHERS_READ,    // 0004
        PosixFilePermission.GROUP_EXECUTE,  // 0010
        PosixFilePermission.GROUP_WRITE,    // 0020
        PosixFilePermission.GROUP_READ,     // 0040
        PosixFilePermission.OWNER_EXECUTE,  // 0100
        PosixFilePermission.OWNER_WRITE,    // 0200
        PosixFilePermission.OWNER_READ      // 0400
    };

    @SneakyThrows
    public static void setPosixFilePermissions(Path target, int mode) {
        if (supportsPosix(target)) {
            Files.setPosixFilePermissions(target, toPosixPermissions(mode));
        }
    }

    public static Set<PosixFilePermission> toPosixPermissions(int mode) {
        EnumSet<PosixFilePermission> perms = EnumSet.noneOf(PosixFilePermission.class);

        for (int i = 0; i < POSIX_PERMISSIONS.length; i++) {
            if ((mode & (1 << i)) != 0) {
                perms.add(POSIX_PERMISSIONS[i]);
            }
        }
        return perms;
    }

    private static boolean supportsPosix(Path path) throws IOException {
        return Files.getFileStore(path).supportsFileAttributeView(PosixFileAttributeView.class);
    }
}
