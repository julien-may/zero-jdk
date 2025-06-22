package dev.zerojdk.infrastructure.unarchiver;

public class UnsupportedArchiveException extends RuntimeException {
    public UnsupportedArchiveException(String message) {
        super(message);
    }
}
