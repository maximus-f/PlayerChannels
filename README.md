# PlayerChannels
This is a complete rewrite of my plugin PrivateTalkPremium. The IF-Lite 0.12.2 build has been tested on Paper 1.21.11 and 26.3 for chest menus, channel chat, and toast notifications.


## How to contribute?
I will be focusing on adding all of the features of PrivateTalkPremium. Create a fork with one of the features you wish to help with and I will check it out, all commits are appreciated.

## Build

Use JDK 17 and Maven 3.9 or newer, then run from the repository root.

```sh
mvn clean verify
```

The plugin is written to `target/PlayerChannels-3.9.jar`. Copy that shaded JAR to each server's `plugins` directory when you want to deploy it. Builds now use `target/` instead of the previous developer-specific server paths.

Maven resolves ToastAPI 2.0 and IF-Lite 0.12.2 under the group ID `io.github.maximus-f` and embeds them in the plugin. To check the build with an empty dependency cache, run the following command with a new temporary directory.

```sh
mvn -Dmaven.repo.local="$(mktemp -d)" clean verify
```
