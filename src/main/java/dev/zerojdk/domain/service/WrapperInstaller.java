package dev.zerojdk.domain.service;

import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.model.WrapperConfig;
import dev.zerojdk.domain.port.out.wrapper.WrapperReleaseLocator;
import dev.zerojdk.domain.port.out.wrapper.WrapperConfigRepository;
import dev.zerojdk.domain.port.out.wrapper.WrapperScriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class WrapperInstaller {
    private final WrapperConfigRepository wrapperConfigRepository;
    private final WrapperScriptRepository wrapperScriptRepository;
    private final WrapperReleaseLocator wrapperReleaseLocator;
    private final WrapperScriptGenerator wrapperScriptGenerator;

    @SneakyThrows
    public void install(Platform platform) {
        WrapperConfig wrapperConfig = wrapperConfigRepository.read()
            .orElseGet(() -> wrapperConfigRepository.write(
                new WrapperConfig(wrapperReleaseLocator.findLatestUrl(platform))));

        wrapperScriptRepository.save(
            wrapperScriptGenerator.generateScript(wrapperConfig.url()));
    }
}
