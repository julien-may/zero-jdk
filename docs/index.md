---
# https://vitepress.dev/reference/default-theme-home-page
layout: home

hero:
  name: "Zero JDK"
  tagline: The zero-dependency JDK bootstrap script
  actions:
    - theme: brand
      text: What is zero-jdk?
      link: /what-is

features:
  - title: One Tool
    details: Bootstrap your Java project with a single POSIX-compliant script - no JDK required beforehand
  - title: One Config File
    details: Use .jdk/config.properties to define your JDK URL. Reproducible, versioned, simple
  - title: Supports Gradle and Maven
    details: Automatically sets up the correct wrapper, so you can run `./build init` and get going
  - title: Runs on Linux and MacOS
    details: Designed and tested on Unix-like systems. No additional tools or global dependencies required
---

## Get Started Now!

Paste and run the following in your terminal:

```sh
curl -sS https://raw.githubusercontent.com/julien-may/zero-jdk/HEAD/install.sh | sh
```

