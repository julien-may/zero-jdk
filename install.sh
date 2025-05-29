#!/bin/sh

readonly ERROR='\033[0;31m'
readonly BOLD='\033[1m'
readonly RESET='\033[0m'

target="build2"

while [[ -e "$target" || -z "$target" ]]; do
  if [ ! -z "$target" ]; then
      printf "${ERROR}warning:${RESET} A file or folder named '${BOLD}$target${RESET}' already exists.\n\n"
  fi

  printf "Enter an alternative name for the script: "
  read INPUT

  target="$INPUT"

  echo ""
done

printf "Downloading... "

curl -fsSL https://raw.githubusercontent.com/julien-may/zero-jdk/HEAD/build -o "$target" || {
  printf "\n${ERROR}error:${RESET} Failed to download the script.\n"
  exit 1
}

chmod +x "$target"

echo "done"
echo ""
echo "Now run the following to get started:"
echo ""
printf "  ${BOLD}./$target init${RESET}\n"
echo ""
