#!/bin/sh
set -e

os=$1
arch=$2

script_dir="$(cd "$(dirname "$0")" && pwd)"
build_path="$script_dir/../build"

temp_dir=$(mktemp -d)
ln -s "$build_path" "$temp_dir/build"

(
    cd "$temp_dir"

    mkdir -p .jdk
    cat > .jdk/config.properties <<EOF
url=https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.7%2B6/OpenJDK21U-jdk_${arch}_${os}_hotspot_21.0.7_6.tar.gz
EOF

    ./build info | grep "21.0.7" || { echo "FAIL: Expected JDK 21.0.7 not found in info output"; exit 1; }
    echo "PASS: JDK 21 is correctly configured and detected"
)
