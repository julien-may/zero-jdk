package dev.zerojdk;

import dev.zerojdk.adapter.in.cli.ZjdkEnv;
import dev.zerojdk.adapter.in.cli.ZjdkUpdate;
import dev.zerojdk.adapter.in.cli.ZjdkList;
import dev.zerojdk.adapter.in.cli.ZjdkInit;
import dev.zerojdk.adapter.in.cli.ZjdkSet;
import dev.zerojdk.adapter.in.cli.ZjdkShell;
import dev.zerojdk.adapter.in.cli.ZjdkSync;
import dev.zerojdk.adapter.in.cli.ZjdkWrapper;

import dev.zerojdk.domain.service.ConfigurationNotFoundException;
import dev.zerojdk.domain.service.UnsupportedIdentifierException;
import dev.zerojdk.infrastructure.configuration.ApplicationContext;
import picocli.CommandLine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST_HEADING;

@CommandLine.Command(name = "zjdk" , mixinStandardHelpOptions = true, footer = "%nSee 'zjdk help <command>' to read about a specific subcommand", commandListHeading = "%nCommands:%n")
public class Application implements Runnable {
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    public static void main(String[] args) {
        ApplicationContext context = new ApplicationContext();

        Map<String, List<String>> sections = new LinkedHashMap<>();
        sections.put("%nBootstrap%n", List.of("init", "sync", "wrapper"));
        sections.put("%nVersion Management%n", List.of("list", "set"));
        sections.put("%nEnvironment%n", List.of("env", "shell"));
        sections.put("%nMaintenance%n", List.of("update"));
        CommandGroupRenderer renderer = new CommandGroupRenderer(sections);

        CommandLine commandLine = new CommandLine(new Application())
            .addSubcommand("init", new ZjdkInit(
                context.getConfigService(),
                context.getManifestSyncService()))
            .addSubcommand("sync", new ZjdkSync(
                context.getManifestSyncService()))
            .addSubcommand("wrapper", new ZjdkWrapper(
                context.getWrapperService()))
            .addSubcommand("set", new CommandLine(new ZjdkSet())
                .addSubcommand("version", new ZjdkSet.Version(
                    context.getConfigService(),
                    context.getManifestSyncService())))
            .addSubcommand("env", new ZjdkEnv(
                context.getConfigService(),
                context.getJdkReleaseService()))
            .addSubcommand("list", new CommandLine(new ZjdkList())
                .addSubcommand("available", new ZjdkList.Available(
                    context.getCatalogRepository())))
            .addSubcommand("shell", new ZjdkShell())
            .addSubcommand("update", new ZjdkUpdate())
            .addSubcommand(new CommandLine.HelpCommand())
            .setExecutionExceptionHandler(new ExecutionExceptionHandler());

        commandLine.getHelpSectionMap().remove(SECTION_KEY_COMMAND_LIST_HEADING);
        commandLine.getHelpSectionMap().put(SECTION_KEY_COMMAND_LIST, renderer);

        System.exit(commandLine.execute(args));
    }

    @Override
    public void run() {
        spec.commandLine().usage(System.err);
    }

    static class ExecutionExceptionHandler implements CommandLine.IExecutionExceptionHandler {

        @Override
        public int handleExecutionException(Exception ex, CommandLine commandLine, CommandLine.ParseResult fullParseResult) {
            if (ex instanceof ConfigurationNotFoundException) {
                System.err.println("zjdk not configured. Try zjdk init...");
            } else if (ex instanceof UnsupportedIdentifierException e) {
                System.err.printf("The defined version %s is not supported", e.getIdentifier());
            } else {
                System.err.println(ex.getMessage());
            }

            return 1;
        }
    }

    static class CommandGroupRenderer implements CommandLine.IHelpSectionRenderer {
        private final Map<String, List<String>> sections;

        public CommandGroupRenderer(Map<String, List<String>> sections) {
            this.sections = sections;
        }

        @Override
        public String render(CommandLine.Help help) {
            if (help.commandSpec().subcommands().isEmpty()) { return ""; }

            StringBuilder result = new StringBuilder();
            sections.forEach((key, value) -> result.append(renderSection(key, value, help)));
            return result.toString();
        }

        private String renderSection(String sectionHeading, List<String> cmdNames, CommandLine.Help help) {
            CommandLine.Help.TextTable textTable = createTextTable(help);

            for (String name : cmdNames) {
                CommandLine.Model.CommandSpec sub = help.commandSpec().subcommands().get(name).getCommandSpec();

                // create comma-separated list of command name and aliases
                String names = sub.names().toString();
                names = names.substring(1, names.length() - 1); // remove leading '[' and trailing ']'

                // description may contain line separators; use Text::splitLines to handle this
                String description = description(sub.usageMessage());
                CommandLine.Help.Ansi.Text[] lines = help.colorScheme().text(String.format(description)).splitLines();

                for (int i = 0; i < lines.length; i++) {
                    CommandLine.Help.Ansi.Text cmdNamesText = help.colorScheme().commandText(i == 0 ? names : "");
                    textTable.addRowValues(cmdNamesText, lines[i]);
                }
            }
            return help.createHeading(sectionHeading) + textTable.toString();
        }

        private CommandLine.Help.TextTable createTextTable(CommandLine.Help help) {
            CommandLine.Model.CommandSpec spec = help.commandSpec();
            // prepare layout: two columns
            // the left column overflows, the right column wraps if text is too long
            int commandLength = maxLength(spec.subcommands(), 37);
            CommandLine.Help.TextTable textTable = CommandLine.Help.TextTable.forColumns(help.colorScheme(),
                new CommandLine.Help.Column(commandLength + 2, 2, CommandLine.Help.Column.Overflow.SPAN),
                new CommandLine.Help.Column(spec.usageMessage().width() - (commandLength + 2), 2, CommandLine.Help.Column.Overflow.WRAP));
            textTable.setAdjustLineBreaksForWideCJKCharacters(spec.usageMessage().adjustLineBreaksForWideCJKCharacters());
            return textTable;
        }

        private int maxLength(Map<String, CommandLine> subcommands, int max) {
            int result = subcommands.values().stream().map(cmd -> cmd.getCommandSpec().names().toString().length() - 2).max(Integer::compareTo).get();
            return Math.min(max, result);
        }

        private String description(CommandLine.Model.UsageMessageSpec usageMessage) {
            if (usageMessage.header().length > 0) {
                return usageMessage.header()[0];
            }
            if (usageMessage.description().length > 0) {
                return usageMessage.description()[0];
            }
            return "";
        }
    }
}
