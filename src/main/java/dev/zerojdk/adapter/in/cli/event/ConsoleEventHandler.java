package dev.zerojdk.adapter.in.cli.event;

import dev.zerojdk.domain.port.out.event.DomainEventObserver;

public interface ConsoleEventHandler {
    void register(DomainEventObserver publisher);
}
