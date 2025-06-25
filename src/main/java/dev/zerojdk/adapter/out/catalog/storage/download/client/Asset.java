package dev.zerojdk.adapter.out.catalog.storage.download.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Asset(String browserDownloadUrl) {
}
