#!/bin/sh
set -e

script_dir="$(cd "$(dirname "$0")" && pwd)"
build_path="$script_dir/../build"

temp_dir=$(mktemp -d)
ln -s "$build_path" "$temp_dir/build"

(
    cd "$temp_dir"
    ./build init mvnw

    [ -f mvnw ] || { echo "FAIL: mvnw missing"; exit 1; }
    [ -f .mvn/wrapper/maven-wrapper.properties ] || { echo "FAIL: Maven wrapper properties missing"; exit 1; }
    echo "PASS: ./build init mvnw installed Maven wrapper"
)
