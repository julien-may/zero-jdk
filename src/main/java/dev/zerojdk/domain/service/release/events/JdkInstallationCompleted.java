package dev.zerojdk.domain.service.release.events;

import dev.zerojdk.domain.model.JdkVersion;
import dev.zerojdk.domain.port.out.event.DomainEvent;

public record JdkInstallationCompleted(JdkVersion version) implements DomainEvent {

}
