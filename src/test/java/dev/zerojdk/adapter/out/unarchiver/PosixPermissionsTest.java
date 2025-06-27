package dev.zerojdk.adapter.out.unarchiver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

class PosixPermissionsTest {
    private static Stream<Arguments> providePermissionsData() {
        return Stream.of(
            Arguments.of(0, EnumSet.noneOf(PosixFilePermission.class)),
            Arguments.of(0b111111111 /* 777 */, EnumSet.allOf(PosixFilePermission.class)),
            Arguments.of(0b110100100 /* 644 */, EnumSet.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.GROUP_READ,
                PosixFilePermission.OTHERS_READ
            )),
            Arguments.of(0b111101101 /* 755 */, EnumSet.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE,
                PosixFilePermission.GROUP_READ,
                PosixFilePermission.GROUP_EXECUTE,
                PosixFilePermission.OTHERS_READ,
                PosixFilePermission.OTHERS_EXECUTE
            )),
            Arguments.of(0b111000000 /* 700 */, EnumSet.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE
            ))
        );
    }

    @ParameterizedTest
    @MethodSource("providePermissionsData")
    void toPosixPermissions(int mode, Set<PosixFilePermission> expectedPermissions) {
        // When
        Set<PosixFilePermission> actualPermissions = PosixPermissions.toPosixPermissions(mode);

        // Then
        assertThat(actualPermissions).isEqualTo(expectedPermissions);
    }

    @Test
    void setPosixFilePermissionsShouldNotSetPermissionsWhenNotSupported() {
        // Given
        Path mockPath = mock(Path.class);
        FileStore mockFileStore = mock(FileStore.class);
        int mode = 0b111101101; // 755 permissions

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.getFileStore(mockPath)).thenReturn(mockFileStore);
            mockedFiles.when(() -> mockFileStore.supportsFileAttributeView(PosixFileAttributeView.class)).thenReturn(false);

            // When
            PosixPermissions.setPosixFilePermissions(mockPath, mode);

            // Then
            mockedFiles.verify(() -> Files.setPosixFilePermissions(any(Path.class), any(Set.class)), never());
        }
    }
}
