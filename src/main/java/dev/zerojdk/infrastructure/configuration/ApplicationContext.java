package dev.zerojdk.infrastructure.configuration;

import dev.zerojdk.adapter.out.catalog.JsonCatalogRepository;
import dev.zerojdk.adapter.out.config.PropertiesConfigRepository;
import dev.zerojdk.adapter.out.download.HttpDownloadService;
import dev.zerojdk.adapter.out.index.PropertiesRegistrationRepository;
import dev.zerojdk.domain.port.out.catalog.CatalogRepository;
import dev.zerojdk.domain.port.out.config.ConfigRepository;
import dev.zerojdk.domain.port.out.download.DownloadService;
import dev.zerojdk.domain.port.out.index.RegistrationRepository;
import dev.zerojdk.domain.service.ConfigService;
import dev.zerojdk.domain.service.JdkReleaseService;
import dev.zerojdk.domain.service.ManifestSyncService;
import dev.zerojdk.infrastructure.unarchiver.UnarchiverFactory;
import lombok.Getter;

import java.io.File;

@Getter
public class ApplicationContext {
    private final CatalogRepository catalogRepository;
    private final ConfigRepository configRepository;
    private final DownloadService downloadService;
    private final UnarchiverFactory unarchiverFactory;
    private final RegistrationRepository registrationRepository;
    private final ConfigService configService;
    private final JdkReleaseService jdkReleaseService;
    private final ManifestSyncService manifestSyncService;

    public ApplicationContext() {
        this.catalogRepository = new JsonCatalogRepository(new File(System.getProperty("user.home"), ".zjdk/catalogue.json"));
        this.configRepository = new PropertiesConfigRepository();
        this.downloadService = new HttpDownloadService();
        this.unarchiverFactory = new UnarchiverFactory();
        this.registrationRepository = new PropertiesRegistrationRepository();
        this.configService = new ConfigService(configRepository, catalogRepository);
        this.jdkReleaseService = new JdkReleaseService(downloadService, unarchiverFactory, catalogRepository, registrationRepository);
        this.manifestSyncService = new ManifestSyncService(catalogRepository, configService, jdkReleaseService);
    }
}
