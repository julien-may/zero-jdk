#!/bin/sh
set -e

# Given
script_dir="$(cd "$(dirname "$0")" && pwd)"
build_path="$script_dir/../build"

temp_dir=$(mktemp -d)
ln -s "$build_path" "$temp_dir/build"

(
    # When
    cd "$temp_dir"
    ./build init

    # Then
    [ -f .jdk/config.properties ] || { echo "FAIL: config.properties missing"; exit 1; }
    [ -d .jdk/releases ] || { echo "FAIL: JDK folder not created"; exit 1; }
    echo "PASS: ./build init created config and JDK"
)
