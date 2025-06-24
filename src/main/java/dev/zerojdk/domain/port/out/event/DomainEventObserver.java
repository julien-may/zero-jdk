package dev.zerojdk.domain.port.out.event;

import java.util.function.Consumer;

public interface DomainEventObserver {
    <T extends DomainEvent> void register(Class<T> eventType, Consumer<T> listener);
}
