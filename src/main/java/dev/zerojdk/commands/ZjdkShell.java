package dev.zerojdk.commands;

import lombok.SneakyThrows;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@CommandLine.Command(header = "Install shell integration scripts for zjdk environment setup", subcommands = ZjdkShell.Install.class)
public class ZjdkShell {
    @CommandLine.Command(name = "install", header = "Install shell integration", subcommands = Install.Zsh.class)
    static class Install {
        @CommandLine.Command(name = "bash", header = "Bash integration")
        static class Bash {

        }

        @CommandLine.Command(name = "zsh", header = "Zsh integration")
        static class Zsh implements Runnable {
            String script = """
                autoload -U add-zsh-hook
                
                _clean_path_of_jdk_bin() {
                  local IFS=: newpath part
                
                  for part in $=PATH; do
                    [[ $part == */.zjdk/*/bin ]] || newpath+=$part:
                  done
                
                  print -r -- "${newpath%:}"
                }
                
                _read_property() {
                  local file=$1 key=$2
                  [[ -r $file ]] || { print -r -- ''; return 1 }
                  local line
                  while IFS= read -r line || [[ -n $line ]]; do
                    # Trim leading/trailing blanks
                    line=${line##[[:space:]]}
                    line=${line%%[[:space:]]}
                
                    # Skip comments / empty lines
                    [[ -z $line || $line == [#\\!]* ]] && continue
                
                    # Split on the first '=' and strip whitespace around key/value
                    k=${line%%=*}
                    v=${line#*=}
                    k=${k//[[:space:]]/}
                    if [[ $k == $key ]]; then
                      v=${v//[[:space:]]/}
                      print -r -- "$v"
                      return 0
                    fi
                  done < "$file"
                }
                
                _find_version_in_tree() {
                  local dir=${1:-$PWD} version
                
                  while [[ $dir != / ]]; do
                    version=$(_read_property "$dir/.zjdk/config.properties" version)
                    [[ -n $version ]] && { print -r -- $version; return }
                
                    dir=${dir:h}
                  done
                }
                
                _default_version() {
                  _read_property "$HOME/.zjdk/config.properties" version
                }
                
                _java_home_for_version() {
                  local version=$1
                  _read_property "$HOME/.zjdk/releases/$version/.info" home
                }
                
                _apply_java_home() {
                  local java_home=$1
                  if [[ -n $java_home ]]; then
                    export JAVA_HOME=$java_home
                    export PATH="$JAVA_HOME/bin:$(_clean_path_of_jdk_bin)"
                  else
                    unset JAVA_HOME
                    export PATH="$(_clean_path_of_jdk_bin)"
                  fi
                }
                
                set_java_home_from_jdk_folder() {
                  # Resolve version.
                  local version=$(_find_version_in_tree "$dir")
                  [[ -z $version ]] && version=$(_default_version)
                
                  # Map version JAVA_HOME.
                  local java_home
                  [[ -n $version ]] && java_home=$(_java_home_for_version "$version")
                
                  _apply_java_home "$java_home"
                }
                
                add-zsh-hook precmd set_java_home_from_jdk_folder
                set_java_home_from_jdk_folder
                """;

            Path extension = Path.of(System.getProperty("user.home"), ".zjdk", "extensions", "zjdk.plugin.zsh");

            @SneakyThrows
            @Override
            public void run() {
                Files.writeString(
                    Files.createDirectories(extension.getParent()),
                    script,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);

                System.out.println(
                    """
                    Add the following line to your ~/.zshrc file:
                        [ -f "$HOME/.zjdk/extensions/zjdk.plugin.zsh" ] && source "$HOME/.zjdk/extensions/zjdk.plugin.zsh"

                    Restart your terminal or execute the above for the settings to take effect.
                    """
                );

            }
        }
    }
}
