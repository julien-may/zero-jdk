package dev.zerojdk.domain.port.out.event;

public interface DomainEventPublisher {
    void publish(DomainEvent domainEvent);
}
