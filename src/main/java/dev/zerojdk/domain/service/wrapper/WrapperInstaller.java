package dev.zerojdk.domain.service.wrapper;

import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.model.wrapper.WrapperConfig;
import dev.zerojdk.domain.port.out.wrapper.WrapperReleaseLocator;
import dev.zerojdk.domain.port.out.wrapper.WrapperConfigRepository;
import dev.zerojdk.domain.port.out.wrapper.WrapperScriptRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WrapperInstaller {
    private final WrapperConfigRepository wrapperConfigRepository;
    private final WrapperScriptRepository wrapperScriptRepository;
    private final WrapperReleaseLocator wrapperReleaseLocator;
    private final WrapperScriptGenerator wrapperScriptGenerator;
    private final BinaryInstaller binaryInstaller;

    public void install(Platform platform) {
        // TODO: The wrapper.properties version/url must be in sync with the binary version

        WrapperConfig wrapperConfig = wrapperConfigRepository.read()
            .orElseGet(() -> wrapperConfigRepository.write(
                new WrapperConfig(wrapperReleaseLocator.findLatestUrl(platform))));

        wrapperScriptRepository.save(
            wrapperScriptGenerator.generateScript(wrapperConfig.url()));

        binaryInstaller.install();
    }
}
