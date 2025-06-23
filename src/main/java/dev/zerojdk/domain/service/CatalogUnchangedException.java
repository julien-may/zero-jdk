package dev.zerojdk.domain.service;

public class CatalogUnchangedException extends RuntimeException {
    public CatalogUnchangedException(String message) {
        super(message);
    }
}