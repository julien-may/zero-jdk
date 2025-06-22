package dev.zerojdk.infrastructure.configuration;

import dev.zerojdk.adapter.out.catalog.JsonCatalogRepository;
import dev.zerojdk.adapter.out.config.PropertiesConfigRepository;
import dev.zerojdk.adapter.out.index.PropertiesRegistrationRepository;
import dev.zerojdk.domain.port.out.download.DownloadService;
import dev.zerojdk.domain.port.out.catalog.CatalogRepository;
import dev.zerojdk.domain.port.out.index.RegistrationRepository;
import dev.zerojdk.domain.service.ConfigService;
import dev.zerojdk.adapter.out.download.HttpDownloadService;
import dev.zerojdk.domain.service.JdkReleaseService;
import dev.zerojdk.infrastructure.unarchiver.UnarchiverFactory;

import java.io.File;

public class BeanConfiguration {
    public static CatalogRepository catalogRepository() {
        return new JsonCatalogRepository(new File(System.getProperty("user.home"), ".zjdk/catalogue.json"));
    }

    public static ConfigService configService() {
        return new ConfigService(new PropertiesConfigRepository(), catalogRepository());
    }

    public static DownloadService downloadService() {
        return new HttpDownloadService();
    }

    public static UnarchiverFactory unarchiverFactory() {
        return new UnarchiverFactory();
    }

    public static JdkReleaseService jdkReleaseService() {
        return new JdkReleaseService(downloadService(), unarchiverFactory(), registrationRepository());
    }

    private static RegistrationRepository registrationRepository() {
        return new PropertiesRegistrationRepository();
    }
}
