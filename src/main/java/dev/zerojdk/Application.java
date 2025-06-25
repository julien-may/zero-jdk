package dev.zerojdk;

import dev.zerojdk.adapter.in.cli.ZjdkEnv;
import dev.zerojdk.adapter.in.cli.ZjdkUpdate;
import dev.zerojdk.adapter.in.cli.ZjdkList;
import dev.zerojdk.adapter.in.cli.ZjdkInit;
import dev.zerojdk.adapter.in.cli.ZjdkSet;
import dev.zerojdk.adapter.in.cli.ZjdkShell;
import dev.zerojdk.adapter.in.cli.ZjdkSync;
import dev.zerojdk.adapter.in.cli.ZjdkWrapper;

import dev.zerojdk.adapter.out.FsBaseLayout;
import dev.zerojdk.adapter.out.SystemPropertyBasedPlatformDetection;
import dev.zerojdk.adapter.out.UnmanagedDirectoryException;
import dev.zerojdk.adapter.out.catalog.storage.FsCatalogStorageLayout;
import dev.zerojdk.adapter.out.catalog.storage.FsCatalogStorageMetadataRepository;
import dev.zerojdk.adapter.out.catalog.storage.download.HttpCatalogDownloadService;
import dev.zerojdk.adapter.out.catalog.JsonCatalogRepository;
import dev.zerojdk.adapter.out.catalog.provider.CatalogStorageProvider;
import dev.zerojdk.adapter.out.catalog.provider.JsonCatalogStorageProvider;
import dev.zerojdk.adapter.out.github.client.DefaultGitHubReleaseClient;
import dev.zerojdk.adapter.out.github.client.GitHubReleaseClient;
import dev.zerojdk.adapter.out.config.FsJdkConfigRepository;
import dev.zerojdk.adapter.out.download.HttpDownloadService;
import dev.zerojdk.adapter.out.event.InMemoryDomainEventPublisher;
import dev.zerojdk.adapter.out.release.FsJdkRegistrationRepository;
import dev.zerojdk.adapter.out.release.FsJdkInstaller;
import dev.zerojdk.adapter.out.release.FsJdkReleaseLayout;
import dev.zerojdk.adapter.out.shell.FsShellExtensionLayout;
import dev.zerojdk.adapter.out.shell.FsShellExtensionStorage;
import dev.zerojdk.adapter.out.wrapper.*;
import dev.zerojdk.domain.port.out.PlatformDetection;
import dev.zerojdk.domain.port.out.BaseLayout;
import dev.zerojdk.domain.port.out.catalog.CatalogDownloadService;
import dev.zerojdk.domain.port.out.catalog.CatalogStorageLayout;
import dev.zerojdk.domain.port.out.catalog.CatalogStorageMetadataRepository;
import dev.zerojdk.domain.port.out.catalog.CatalogRepository;
import dev.zerojdk.domain.port.out.config.JdkConfigRepository;
import dev.zerojdk.domain.port.out.download.DownloadService;
import dev.zerojdk.domain.port.out.release.JdkRegistrationRepository;
import dev.zerojdk.domain.port.out.release.JdkInstaller;
import dev.zerojdk.domain.port.out.release.JdkReleaseLayout;
import dev.zerojdk.domain.port.out.shell.ShellExtensionLayout;
import dev.zerojdk.domain.port.out.shell.ShellExtensionStorage;
import dev.zerojdk.domain.port.out.wrapper.*;
import dev.zerojdk.adapter.out.unarchiver.DetectingUnarchiverFactory;
import dev.zerojdk.domain.service.catalog.CatalogService;
import dev.zerojdk.domain.service.catalog.CatalogStorageService;
import dev.zerojdk.domain.service.config.JdkConfigService;
import dev.zerojdk.domain.service.config.UnsupportedIdentifierException;
import dev.zerojdk.domain.service.release.JdkReleaseService;
import dev.zerojdk.domain.service.shell.ShellExtensionWriter;
import dev.zerojdk.domain.service.sync.ManifestSyncService;
import dev.zerojdk.domain.service.wrapper.WrapperInstaller;
import dev.zerojdk.domain.service.wrapper.WrapperScriptGenerator;
import dev.zerojdk.infrastructure.VersionProvider;
import picocli.CommandLine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST_HEADING;

@CommandLine.Command(name = "zjdk" ,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    footer = "%nSee 'zjdk help <command>' to read about a specific subcommand",
    commandListHeading = "%nCommands:%n")
public class Application implements Runnable {
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    public static void main(String[] args) {
        // Common
        PlatformDetection platformDetection = new SystemPropertyBasedPlatformDetection();
        BaseLayout baseLayout = new FsBaseLayout();
        DownloadService downloadService = new HttpDownloadService();
        DetectingUnarchiverFactory unarchiverFactory = new DetectingUnarchiverFactory();

        // Event Management
        InMemoryDomainEventPublisher domainEventPublisher = new InMemoryDomainEventPublisher();

        // GitHub Client
        GitHubReleaseClient gitHubReleaseClient = new DefaultGitHubReleaseClient(downloadService);

        // Catalog Storage setup
        CatalogStorageLayout catalogStorageLayout = new FsCatalogStorageLayout(baseLayout);
        CatalogStorageMetadataRepository catalogStorageMetadataRepository = new FsCatalogStorageMetadataRepository(catalogStorageLayout);

        CatalogDownloadService catalogDownloadService = new HttpCatalogDownloadService(gitHubReleaseClient, unarchiverFactory);
        CatalogStorageService catalogStorageService = new CatalogStorageService(catalogDownloadService, catalogStorageMetadataRepository);
        CatalogStorageProvider catalogStorageProvider = new JsonCatalogStorageProvider(catalogStorageService);

        // Catalog
        CatalogRepository catalogRepository = new JsonCatalogRepository(catalogStorageProvider);
        CatalogService catalogService = new CatalogService(catalogRepository);

        // JDK Config
        JdkConfigRepository jdkConfigRepository = new FsJdkConfigRepository(baseLayout);
        JdkConfigService jdkConfigService = new JdkConfigService(jdkConfigRepository, catalogService);

        // Jdk Release
        JdkReleaseLayout jdkReleaseLayout = new FsJdkReleaseLayout(baseLayout);
        JdkRegistrationRepository jdkRegistrationRepository = new FsJdkRegistrationRepository(jdkReleaseLayout);
        JdkInstaller jdkInstaller = new FsJdkInstaller(jdkRegistrationRepository);
        JdkReleaseService jdkReleaseService = new JdkReleaseService(domainEventPublisher, jdkReleaseLayout, downloadService,
            unarchiverFactory, catalogService, jdkRegistrationRepository, jdkInstaller);

        // Sync aggregation
        ManifestSyncService manifestSyncService = new ManifestSyncService(catalogService, jdkConfigService, jdkReleaseService);

        // Wrapper
        WrapperLayout wrapperLayout = new FsWrapperLayout(baseLayout);
        WrapperConfigRepository wrapperConfigRepository = new FsWrapperConfigRepository(wrapperLayout);
        WrapperScriptRepository wrapperScriptRepository = new FsWrapperScriptRepository(wrapperLayout);
        WrapperReleaseLocator wrapperReleaseLocator = new WrapperReleaseLocatorAdapter(gitHubReleaseClient);
        WrapperScriptGenerator wrapperScriptGenerator = new WrapperScriptGenerator(baseLayout, wrapperLayout);
        WrapperInstaller wrapperInstaller = new WrapperInstaller(wrapperConfigRepository, wrapperScriptRepository, wrapperReleaseLocator, wrapperScriptGenerator);

        // Shell Extensions
        ShellExtensionLayout shellExtensionLayout = new FsShellExtensionLayout(baseLayout);
        ShellExtensionStorage shellExtensionStorage = new FsShellExtensionStorage(shellExtensionLayout);
        ShellExtensionWriter shellExtensionWriter = new ShellExtensionWriter(shellExtensionStorage);

        // CLI setup
        CommandLine commandLine = new CommandLine(new Application())
            .addSubcommand("init", new ZjdkInit(platformDetection, jdkConfigService, manifestSyncService, domainEventPublisher))
            .addSubcommand("sync", new ZjdkSync(platformDetection, manifestSyncService, domainEventPublisher))
            .addSubcommand("wrapper", new ZjdkWrapper(platformDetection, wrapperInstaller))
            .addSubcommand("set", new CommandLine(new ZjdkSet())
                .addSubcommand("version", new ZjdkSet.Version(platformDetection, jdkConfigService, manifestSyncService, domainEventPublisher)))
            .addSubcommand("env", new ZjdkEnv(platformDetection, jdkConfigService, jdkReleaseService))
            .addSubcommand("list", new CommandLine(new ZjdkList())
                .addSubcommand("available", new ZjdkList.Available(platformDetection, catalogService)))
            .addSubcommand("shell", new CommandLine(new ZjdkShell())
                .addSubcommand("install", new CommandLine(new ZjdkShell.Install())
                    .addSubcommand("zsh", new ZjdkShell.Install.Zsh(shellExtensionWriter))))
            .addSubcommand("update", new ZjdkUpdate(catalogStorageService))
            .addSubcommand(new CommandLine.HelpCommand())
            .setExecutionExceptionHandler(new ExecutionExceptionHandler());

        // Help page rendering
        Map<String, List<String>> sections = new LinkedHashMap<>();
        sections.put("%nBootstrap%n", List.of("init", "sync", "wrapper"));
        sections.put("%nVersion Management%n", List.of("list", "set"));
        sections.put("%nEnvironment%n", List.of("env", "shell"));
        sections.put("%nMaintenance%n", List.of("update"));
        CommandGroupRenderer renderer = new CommandGroupRenderer(sections);

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
            if (ex instanceof UnsupportedIdentifierException e) {
                System.err.printf("The defined version %s is not supported\n", e.getIdentifier());
            } else if (ex instanceof UnmanagedDirectoryException) {
                System.err.println("Not a zero-jdk managed directory (or any of the parent directories): .zjdk");
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
