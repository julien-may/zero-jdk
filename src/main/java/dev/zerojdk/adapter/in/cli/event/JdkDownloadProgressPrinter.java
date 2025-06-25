package dev.zerojdk.adapter.in.cli.event;

import dev.zerojdk.domain.port.out.event.DomainEventObserver;
import dev.zerojdk.domain.service.release.events.JdkDownloadProgress;
import dev.zerojdk.domain.service.release.events.JdkDownloadStarted;

public class JdkDownloadProgressPrinter implements ConsoleEventHandler {
    @Override
    public void register(DomainEventObserver observer) {
        final String CSI = "\u001B[";

        observer.register(JdkDownloadStarted.class, e -> {
                System.out.printf("Downloading: %s... ", e.version().getIdentifier());
                System.out.print(CSI + "s");
                System.out.flush();
            }
        );

        observer.register(JdkDownloadProgress.class, e -> {
            System.out.print(CSI + "u");
            System.out.printf("%3d%%", e.bytesRead() * 100 / e.totalBytes());
            System.out.flush();

            if (e.bytesRead() >= e.totalBytes()) {
                System.out.println();
            }
        });
    }
}