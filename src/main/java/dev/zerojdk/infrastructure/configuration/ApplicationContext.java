package dev.zerojdk.infrastructure.configuration;

import dev.zerojdk.adapter.out.RecursiveLayoutLocator;
import dev.zerojdk.adapter.out.catalog.JsonCatalogRepository;
import dev.zerojdk.adapter.out.catalog.provider.JsonCatalogStorageProvider;
import dev.zerojdk.adapter.out.config.FsConfigRepository;
import dev.zerojdk.adapter.out.download.HttpDownloadService;
import dev.zerojdk.adapter.out.index.FsRegistrationRepository;
import dev.zerojdk.adapter.out.wrapper.FsWrapperBinaryRepository;
import dev.zerojdk.adapter.out.wrapper.FsWrapperConfigRepository;
import dev.zerojdk.adapter.out.wrapper.FsWrapperScriptRepository;
import dev.zerojdk.adapter.out.wrapper.WrapperReleaseLocatorAdapter;
import dev.zerojdk.domain.port.out.ProjectLayoutPort;
import dev.zerojdk.domain.port.out.catalog.CatalogRepository;
import dev.zerojdk.domain.port.out.config.ConfigRepository;
import dev.zerojdk.domain.port.out.download.DownloadService;
import dev.zerojdk.domain.port.out.index.RegistrationRepository;
import dev.zerojdk.domain.port.out.wrapper.WrapperReleaseLocatorPort;
import dev.zerojdk.domain.port.out.wrapper.WrapperBinaryStorePort;
import dev.zerojdk.domain.port.out.wrapper.WrapperConfigStorePort;
import dev.zerojdk.domain.port.out.wrapper.WrapperScriptStorePort;
import dev.zerojdk.domain.service.*;
import dev.zerojdk.infrastructure.unarchiver.UnarchiverFactory;
import lombok.Getter;

@Getter
public class ApplicationContext {
    private final ProjectLayoutPort projectLayoutPort;
    private final CatalogRepository catalogRepository;
    private final CatalogService catalogService;
    private final ConfigRepository configRepository;
    private final DownloadService downloadService;
    private final UnarchiverFactory unarchiverFactory;
    private final RegistrationRepository registrationRepository;
    private final ConfigService configService;
    private final JdkReleaseService jdkReleaseService;
    private final ManifestSyncService manifestSyncService;
    private final WrapperService wrapperService;

    private final WrapperBinaryStorePort wrapperBinaryStorePort;
    private final WrapperConfigStorePort wrapperConfigStorePort;
    private final WrapperScriptStorePort wrapperScriptStorePort;
    private final WrapperReleaseLocatorPort wrapperReleaseLocatorPort;

    public ApplicationContext() {
        this.projectLayoutPort = new RecursiveLayoutLocator();
        this.downloadService = new HttpDownloadService();
        this.unarchiverFactory = new UnarchiverFactory();
        this.catalogRepository = new JsonCatalogRepository(new JsonCatalogStorageProvider(downloadService, unarchiverFactory));
        this.catalogService = new CatalogService(catalogRepository);
        this.configRepository = new FsConfigRepository(projectLayoutPort);
        this.registrationRepository = new FsRegistrationRepository();
        this.configService = new ConfigService(configRepository, catalogService);
        this.jdkReleaseService = new JdkReleaseService(downloadService, unarchiverFactory, catalogService, registrationRepository);
        this.manifestSyncService = new ManifestSyncService(catalogService, configService, jdkReleaseService);

        this.wrapperBinaryStorePort = new FsWrapperBinaryRepository(projectLayoutPort);
        this.wrapperConfigStorePort = new FsWrapperConfigRepository(projectLayoutPort);
        this.wrapperScriptStorePort = new FsWrapperScriptRepository(projectLayoutPort);
        this.wrapperReleaseLocatorPort = new WrapperReleaseLocatorAdapter();

        this.wrapperService = new WrapperService(wrapperBinaryStorePort, wrapperConfigStorePort, wrapperScriptStorePort, wrapperReleaseLocatorPort, projectLayoutPort);

    }
}
