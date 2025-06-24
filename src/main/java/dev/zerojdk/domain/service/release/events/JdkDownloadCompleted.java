package dev.zerojdk.domain.service.release.events;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.event.DomainEvent;

public record JdkDownloadCompleted(JdkVersion version) implements DomainEvent {

}
