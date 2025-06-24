package dev.zerojdk.domain.service.config;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UnsupportedIdentifierException extends RuntimeException {
    private final String identifier;
}
