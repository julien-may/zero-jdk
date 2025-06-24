package dev.zerojdk.domain.service.wrapper;

import dev.zerojdk.domain.port.out.BaseLayout;
import dev.zerojdk.domain.port.out.wrapper.WrapperLayout;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

@RequiredArgsConstructor
public class WrapperScriptGenerator {
    private final BaseLayout baseLayout;
    private final WrapperLayout wrapperLayout;

    public String generateScript(String downloadUrl) {
        Path binPath = wrapperLayout.binaryPath();

        return """
            #!/usr/bin/env sh
            set -eu

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
                    baseLayout.discoverProjectRoot()
                        .map(root -> root.relativize(wrapperLayout.ensureWrapperDirectory()))
                        // TODO: concrete Exception
                        .orElseThrow(),
                    binPath.getFileName().toString(),
                    wrapperLayout.configPath().getFileName(),
                    downloadUrl);
    }
}
