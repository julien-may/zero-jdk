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

    PRE_JAVA_HOME=$(./build info | grep JAVA_HOME)

    cat > .jdk/config.properties <<EOF
url=https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.11%2B9/OpenJDK17U-jdk_${arch}_${os}_hotspot_17.0.11_9.tar.gz
EOF

    POST_JAVA_HOME=$(./build info | grep JAVA_HOME)

    [ "$PRE_JAVA_HOME" != "$POST_JAVA_HOME" ] || { echo "FAIL: JAVA_HOME did not change after config change"; exit 1; }

    ./build info | grep "17.0.11" || { echo "FAIL: Expected JDK 17.0.11 not found after update"; exit 1; }

    echo "PASS: Changing config triggered new JDK setup"
)
