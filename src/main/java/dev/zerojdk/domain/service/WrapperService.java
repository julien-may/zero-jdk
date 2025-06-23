package dev.zerojdk.domain.service;

import dev.zerojdk.domain.model.Platform;
import dev.zerojdk.domain.model.WrapperConfig;
import dev.zerojdk.domain.port.out.ProjectLayout;
import dev.zerojdk.domain.port.out.wrapper.WrapperReleaseLocator;
import dev.zerojdk.domain.port.out.wrapper.WrapperBinaryRepository;
import dev.zerojdk.domain.port.out.wrapper.WrapperConfigRepository;
import dev.zerojdk.domain.port.out.wrapper.WrapperScriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Path;

@RequiredArgsConstructor
public class WrapperService {
    private final WrapperBinaryRepository wrapperBinaryRepository;
    private final WrapperConfigRepository wrapperConfigRepository;
    private final WrapperScriptRepository wrapperScriptRepository;
    private final WrapperReleaseLocator wrapperReleaseLocator;
    private final ProjectLayout projectLayout;

    @SneakyThrows
    public void install(Platform platform) {
        // IMPROVEMENT: This right now uses the latest version, but maybe it makes sense to use the same version as the one with which
        // the wrapper was generated...
        WrapperConfig wrapperConfig = wrapperConfigRepository.read().orElseGet(() -> {
            WrapperConfig config = new WrapperConfig(wrapperReleaseLocator.findLatestUrl(platform));
            wrapperConfigRepository.write(config);
            return config;
        });

        wrapperScriptRepository.save(buildShellScript(wrapperConfig.url()));
    }

    private String buildShellScript(String downloadUrl) {
        Path projectRoot = projectLayout.findProjectRoot(false)
            .orElseThrow(); // TODO

        Path relativizePathOfBinary = projectRoot.relativize(wrapperBinaryRepository.executable());

        return """
            #!/usr/bin/env sh
            set -euo pipefail

            WRAPPER_DIR="$(cd "$(dirname "$0")/%s" && pwd)"
            BIN="$WRAPPER_DIR/%s"
            PROPS="$WRAPPER_DIR/%s"

            if [ ! -f "$BIN" ]; then
              if [ -f "$PROPS" ]; then
                # Read and unescape the URL for shell use
                url=$(sed -n 's/^url=//p' "$PROPS" | sed 's/\\\\:/:/g')
              else
                url=%s
                echo "url=$url" > "$PROPS"
              fi

              tmpdir=$(mktemp -d)
              cleanup() { rm -rf "$tmpdir"; }
              trap cleanup EXIT

              echo "Downloading $url …" >&2
              case $url in
                *.tar.gz|*.tgz)
                  curl -fsSL "$url" | tar -xzf - -C "$tmpdir"
                  extracted_bin="$(find "$tmpdir" -type f -name zjdk -perm +111 | head -n 1)"
                  if [ -z "$extracted_bin" ]; then
                    echo "Error: No zjdk binary found in archive" >&2
                    exit 1
                  fi
                  mv "$extracted_bin" "$BIN"
                  chmod +x "$BIN"
                  ;;
                *)
                  echo "Unsupported archive format: $url" >&2
                  exit 1
                  ;;
              esac
            fi

            exec "$BIN" "$@"
            """.formatted(
                relativizePathOfBinary.getParent(),
                relativizePathOfBinary.getFileName(),
                wrapperConfigRepository.propertiesFileName(),
                downloadUrl);
    }
}
