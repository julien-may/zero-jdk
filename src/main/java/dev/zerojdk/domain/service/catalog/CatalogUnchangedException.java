package dev.zerojdk.domain.service.catalog;

public class CatalogUnchangedException extends RuntimeException {
    public CatalogUnchangedException(String message) {
        super(message);
    }
}