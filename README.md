# PlayerChannels
This is a complete rewrite of my plugin PrivateTalkPremium. The rewritten version supports only the latest 1.14 and up. Support for 1.13 will also be added at a later date.


## How to contribute?
I will be focusing on adding all of the features of PrivateTalkPremium. Create a fork with one of the features you wish to help with and I will check it out, all commits are appreciated.

## Build

Use JDK 17 and Maven 3.9 or newer, then run from the repository root.

```sh
mvn clean verify
```

The plugin is written to `target/PlayerChannels-3.8.2.jar`. Copy that shaded JAR to each server's `plugins` directory when you want to deploy it. Builds now use `target/` instead of the previous developer-specific server paths.

ToastAPI and IF-Lite are resolved from the checked-in `vendor/maven` repository. Their exact binaries, provenance and checksums are documented in [vendor/README.md](vendor/README.md). Other dependencies are downloaded by Maven. To check the build with an empty dependency cache, run the following command with a new temporary directory.

```sh
mvn -Dmaven.repo.local="$(mktemp -d)" clean verify
```
