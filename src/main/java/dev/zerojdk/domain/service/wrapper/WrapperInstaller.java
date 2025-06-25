package dev.zerojdk.domain.service.wrapper;

import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.model.wrapper.WrapperConfig;
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

        // TODO: instead of only creating the script we should also copy the version of zjdk that is currently
        //  being executed into the directory

        wrapperScriptRepository.save(
            wrapperScriptGenerator.generateScript(wrapperConfig.url()));
    }
}
