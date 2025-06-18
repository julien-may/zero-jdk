package dev.zerojdk;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
public class UnsupportedIdentifierException extends RuntimeException {
    private final String identifier;
}
