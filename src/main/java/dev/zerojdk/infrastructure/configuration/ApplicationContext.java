package dev.zerojdk.infrastructure.configuration;

import dev.zerojdk.adapter.out.RecursiveLayoutLocator;
import dev.zerojdk.adapter.out.catalog.FsCatalogMetadataRepository;
import dev.zerojdk.adapter.out.catalog.JsonCatalogRepository;
import dev.zerojdk.adapter.out.catalog.provider.CatalogStorageProvider;
import dev.zerojdk.adapter.out.catalog.provider.JsonCatalogStorageProvider;
import dev.zerojdk.adapter.out.config.FsConfigRepository;
import dev.zerojdk.adapter.out.download.HttpDownloadService;
import dev.zerojdk.adapter.out.index.FsRegistrationRepository;
import dev.zerojdk.adapter.out.wrapper.FsWrapperBinaryRepository;
import dev.zerojdk.adapter.out.wrapper.FsWrapperConfigRepository;
import dev.zerojdk.adapter.out.wrapper.FsWrapperScriptRepository;
import dev.zerojdk.adapter.out.wrapper.WrapperReleaseLocatorAdapter;
import dev.zerojdk.domain.port.out.ProjectLayout;
import dev.zerojdk.domain.port.out.catalog.CatalogMetadataRepository;
import dev.zerojdk.domain.port.out.catalog.CatalogRepository;
import dev.zerojdk.domain.port.out.config.ConfigRepository;
import dev.zerojdk.domain.port.out.download.DownloadService;
import dev.zerojdk.domain.port.out.index.RegistrationRepository;
import dev.zerojdk.domain.port.out.wrapper.WrapperReleaseLocator;
import dev.zerojdk.domain.port.out.wrapper.WrapperBinaryRepository;
import dev.zerojdk.domain.port.out.wrapper.WrapperConfigRepository;
import dev.zerojdk.domain.port.out.wrapper.WrapperScriptRepository;
import dev.zerojdk.domain.service.*;
import dev.zerojdk.domain.service.HttpCatalogDownloadService;
import dev.zerojdk.infrastructure.unarchiver.UnarchiverFactory;
import lombok.Getter;

@Getter
public class ApplicationContext {
    private final ProjectLayout projectLayout;
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

    private final CatalogMetadataRepository catalogMetadataRepository;
    private final CatalogDownloadService catalogDownloadService;

    private final WrapperBinaryRepository wrapperBinaryRepository;
    private final WrapperConfigRepository wrapperConfigRepository;
    private final WrapperScriptRepository wrapperScriptRepository;
    private final WrapperReleaseLocator wrapperReleaseLocator;

    private final CatalogStorageProvider catalogStorageProvider;

    public ApplicationContext() {
        this.projectLayout = new RecursiveLayoutLocator();
        this.downloadService = new HttpDownloadService();
        this.unarchiverFactory = new UnarchiverFactory();
        this.catalogDownloadService = new HttpCatalogDownloadService(downloadService, unarchiverFactory);
        this.catalogMetadataRepository = new FsCatalogMetadataRepository();
        this.catalogRepository = new JsonCatalogRepository(new JsonCatalogStorageProvider(catalogDownloadService, catalogMetadataRepository));
        this.catalogService = new CatalogService(catalogRepository);
        this.configRepository = new FsConfigRepository(projectLayout);
        this.registrationRepository = new FsRegistrationRepository();
        this.configService = new ConfigService(configRepository, catalogService);
        this.jdkReleaseService = new JdkReleaseService(downloadService, unarchiverFactory, catalogService, registrationRepository);
        this.manifestSyncService = new ManifestSyncService(catalogService, configService, jdkReleaseService);

        this.wrapperBinaryRepository = new FsWrapperBinaryRepository(projectLayout);
        this.wrapperConfigRepository = new FsWrapperConfigRepository(projectLayout);
        this.wrapperScriptRepository = new FsWrapperScriptRepository(projectLayout);
        this.wrapperReleaseLocator = new WrapperReleaseLocatorAdapter();

        this.wrapperService = new WrapperService(wrapperBinaryRepository, wrapperConfigRepository, wrapperScriptRepository, wrapperReleaseLocator, projectLayout);

        this.catalogStorageProvider = new JsonCatalogStorageProvider(catalogDownloadService, catalogMetadataRepository);
    }
}
