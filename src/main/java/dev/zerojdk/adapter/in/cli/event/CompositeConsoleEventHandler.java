package dev.zerojdk.adapter.in.cli.event;

import dev.zerojdk.domain.port.out.event.DomainEventObserver;

import java.util.List;

public class CompositeConsoleEventHandler implements ConsoleEventHandler {
    private final List<ConsoleEventHandler> handlers;

    public CompositeConsoleEventHandler(ConsoleEventHandler... handlers) {
        this.handlers = List.of(handlers);
    }

    @Override
    public void register(DomainEventObserver observer) {
        handlers.forEach(handler -> handler.register(observer));
    }
}
