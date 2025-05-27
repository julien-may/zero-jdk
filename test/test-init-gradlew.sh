#!/bin/sh
set -e

script_dir="$(cd "$(dirname "$0")" && pwd)"
build_path="$script_dir/../build"

temp_dir=$(mktemp -d)
ln -s "$build_path" "$temp_dir/build"

(
    cd "$temp_dir"
    ./build init gradlew

    [ -f gradle/wrapper/gradle-wrapper.jar ] || { echo "FAIL: gradle-wrapper.jar missing"; exit 1; }
    [ -f gradle/wrapper/gradle-wrapper.properties ] || { echo "FAIL: gradle-wrapper.properties missing"; exit 1; }
    echo "PASS: ./build init gradlew installed Gradle wrapper"
)
