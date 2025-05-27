# Zero JDK

Zero JDK is a lightweight build bootstrapper for Java projects. It ensures that the required JDK version is downloaded and used automatically, without requiring a globally installed Java runtime. It also supports Maven and Gradle wrapper setup with simple commands.

![Image](https://github.com/user-attachments/assets/0c782a11-94d0-49a5-aa3d-aca908d5147f)

## Motivation
Setting up a new Java project often requires preinstalled tools such as:

- A specific version of the JDK
- Maven or Gradle wrappers

This can introduce friction for new contributors, CI pipelines, or isolated environments. It becomes even more cumbersome when working on multiple projects that require different JDK versions.

**Zero JDK simplifies this process** by allowing developers to:

- Use a project-specific JDK without touching global system settings
- Set up Maven or Gradle wrappers without requiring them to be installed globally
- Reproduce builds based solely on project configuration

## Supported Systems

Zero JDK currently supports:

- macOS (x64 and Apple Silicon)
- Linux (x64 and ARM64)

Tested with:

- Bash and POSIX-compliant `sh`
- curl, tar, unzip, file (common UNIX utilities)

## Installation

Run this in the root of your Java project:

```sh
curl -fsSL https://raw.githubusercontent.com/julien-may/zero-jdk/HEAD/build -o build && chmod +x build
```

You now have a local `./build` script ready to use.

## Usage

### Initialize project (JDK + optional wrappers):

```sh
./build init              # Initializes the JDK. Default is Temurin 21
./build init mvnw         # Adds the Maven wrapper
./build init gradlew      # Adds the Gradle wrapper
```

### Run your build tool (uses downloaded JDK):

```sh
./build run clean install     # Runs ./mvnw or ./gradlew if found, passing all arguments
```

### Show current JDK in use:

```sh
./build info
```

### Help:

```sh
./build help
./build help init
```

## Configuration

The `.jdk/config.properties` file defines the JDK to use:

```properties
url=https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.7%2B6/OpenJDK21U-jdk_x64_linux_hotspot_21.0.7_6.tar.gz
```

Update this file to switch to a different JDK version.

To override trusted source validation (e.g. for internal builds):

```sh
./build init --force
```
