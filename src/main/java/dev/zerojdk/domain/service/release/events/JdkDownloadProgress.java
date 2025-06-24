package dev.zerojdk.domain.service.release.events;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.event.DomainEvent;

public record JdkDownloadProgress(JdkVersion version, long bytesRead, long totalBytes) implements DomainEvent {
}
