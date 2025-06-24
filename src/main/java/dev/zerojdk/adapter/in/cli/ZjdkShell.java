package dev.zerojdk.adapter.in.cli;

import dev.zerojdk.domain.service.ShellExtensionWriter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import picocli.CommandLine;

import java.nio.file.Path;

@CommandLine.Command(header = "Install shell integration scripts for zjdk environment setup")
public class ZjdkShell {
    @CommandLine.Command(header = "Install shell integration")
    public static class Install {
        @CommandLine.Command(name = "bash", header = "Bash integration")
        static class Bash {

        }

        @RequiredArgsConstructor
        @CommandLine.Command(header = "Zsh integration")
        public static class Zsh implements Runnable {
            private final ShellExtensionWriter shellExtensionWriter;

            @SneakyThrows
            @Override
            public void run() {
                Path path = shellExtensionWriter.writeZshIntegrationScript();
                Path relativePath = Path.of(System.getProperty("user.home")).relativize(path);

                System.out.printf("""
                    Add the following line to your ~/.zshrc file:
                        [ -f "$HOME/%s" ] && source "$HOME/%s"
                    
                    Restart your terminal or execute the above for the settings to take effect.
                    """, relativePath, relativePath);

            }
        }
    }
}
