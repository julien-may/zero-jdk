#!/bin/sh
set -e

script_dir="$(cd "$(dirname "$0")" && pwd)"
build_path="$script_dir/../build"

temp_dir=$(mktemp -d)
ln -s "$build_path" "$temp_dir/build"

(
    cd "$temp_dir"
    ./build init > /dev/null
    INFO=$(./build info)

    echo "$INFO" | grep "JAVA_HOME" || { echo "FAIL: JAVA_HOME not printed"; exit 1; }
    echo "$INFO" | grep "version" || { echo "FAIL: java -version not printed"; exit 1; }
    echo "PASS: ./build info output verified"
)
