package dev.zerojdk.infrastructure.configuration;

import dev.zerojdk.adapter.out.catalog.json.JsonCatalogRepository;
import dev.zerojdk.adapter.out.config.PropertiesConfigRepository;
import dev.zerojdk.domain.port.out.catalog.CatalogRepository;
import dev.zerojdk.domain.service.ConfigService;
import dev.zerojdk.domain.service.DownloadService;
import dev.zerojdk.domain.service.JdkReleaseService;

import java.io.File;

public class BeanConfiguration {
    public static CatalogRepository catalogRepository() {
        return new JsonCatalogRepository(new File(System.getProperty("user.home"), ".zjdk/catalogue.json"));
    }

    public static ConfigService configService() {
        return new ConfigService(new PropertiesConfigRepository(), catalogRepository());
    }

    public static DownloadService downloadService() {
        return new DownloadService();
    }

    public static JdkReleaseService jdkReleaseService() {
        return new JdkReleaseService(downloadService());
    }
}
