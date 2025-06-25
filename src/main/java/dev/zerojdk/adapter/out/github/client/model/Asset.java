package dev.zerojdk.adapter.out.github.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Asset(String browserDownloadUrl) {
}
