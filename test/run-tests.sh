#!/bin/sh
set -e

TEST_DIR="$(cd "$(dirname "$0")" && pwd)"

pass_count=0
fail_count=0

echo "Running ZeroJDK tests..."

os="$(uname | tr '[:upper:]' '[:lower:]')"
case "$os" in
    darwin) os="mac" ;;
    linux)  os="linux" ;;
    *)
        echo "Error: Unsupported OS: $os" >&2
        return 1
        ;;
esac

arch="$(uname -m)"
case "$arch" in
    x86_64) arch="x64" ;;
    aarch64|arm64) arch="aarch64" ;;
    arm*) arch="arm" ;;
    *)
        echo "Error: Unsupported architecture: $arch" >&2
        return 1
        ;;
esac

for test_file in "$TEST_DIR"/test-*.sh; do
  [ "$test_file" = "$TEST_DIR/run-tests.sh" ] && continue
  echo "--- Running $(basename "$test_file")"
  if sh "$test_file" "$os" "$arch"; then
    pass_count=$((pass_count + 1))
  else
    fail_count=$((fail_count + 1))
  fi
  echo
done

echo "Tests completed: $pass_count passed, $fail_count failed."
[ "$fail_count" -eq 0 ] || exit 1
