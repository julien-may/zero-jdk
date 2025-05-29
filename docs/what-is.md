# What is Zero JDK?

**Zero JDK** is a minimal, POSIX-compliant shell script that makes onboarding and CI for Java projects effortless. It bootstraps your environment by downloading the JDK specified in a simple config file, then installs Maven or Gradle wrappers - all without requiring Java to be installed globally beforehand.

It was built to solve a common problem: starting a Java project without having to first install a JDK globally, as well as handling the complexity of switching between projects that rely on different JDK versions. It also enables CI environments to be set up with zero assumptions.

With Zero JDK, you simply clone the repository, run `./build init`, and your JDK will be downloaded, extracted, and configured for your environment. If you want to set up a specific build tool, you can run `./build init mvnw` or `./build init gradlew` to install the appropriate wrapper.

Currently, the JDK must be specified as a URL pointing to a `.tar.gz` archive that matches your platform. If no configuration is provided, Zero JDK defaults to installing the Temurin JDK 21 LTS for your system. In the future, helper functionality may guide you interactively through selecting and configuring a JDK.

The long-term goal of Zero JDK is to provide a seamless, consistent developer experience by keeping JDK resolution and project setup fully encapsulated inside your project. This includes support for multiple build tools such as Maven and Gradle, so everything runs through the `./build` script with the correct environment automatically set up.

Think of it as `nvm`, `rbenv`, or `asdf` but tailored for Java developers who want project-local control, reproducibility, and zero global dependencies.
