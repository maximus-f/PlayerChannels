# PlayerChannels

A rewrite of PrivateTalkPremium providing private player chat channels.

## Compatibility review (2026-10-31)

Latest Spigot release checked: **26.3** ([release metadata](https://hub.spigotmc.org/versions/26.3.json)). Its metadata requires Java class versions 69–70 (Java 25–26). The published API is `26.3-R0.1-SNAPSHOT` ([API metadata](https://hub.spigotmc.org/nexus/content/repositories/snapshots/org/spigotmc/spigot-api/maven-metadata.xml)).

**Compatibility with Spigot/Minecraft 26.3 has not been verified.** The previous blanket claim of support for 1.14 and up should not be treated as a tested support matrix.

| Component | Current setting | Review finding |
| --- | --- | --- |
| Plugin release | 3.8.2 | Maven and the filtered plugin descriptor now share one version. |
| Compile API | Spigot 1.20.6-R0.1-SNAPSHOT | Does not establish compatibility with 26.3. |
| Java source/target | 8 | Bytecode target, not a claim that modern servers run on Java 8. |
| plugin.yml api-version | 1.14 | Minimum API declaration, not the latest tested server version. |
| UltimateAdvancementAPI | 2.4.3 | Published JAR contains version-specific adapters through v1_21_R3; no 26.3 adapter was found. The plugin calls this library from onLoad and onEnable, so startup and toast behavior require verification. |
| InventoryFramework IF-Lite | 0.10.19 | Baseline Maven package fails because this artifact cannot be resolved from the configured repositories or Maven Central. |

### Validation and remaining work

A baseline `mvn -DskipTests package` attempt using Maven 3.9.11 and Java 21 failed during dependency resolution for IF-Lite 0.10.19, before compilation. No live server smoke test was performed.

Before advertising 26.3 support:

1. Restore a resolvable InventoryFramework dependency and verify inventory behavior.
2. Select and verify an advancement/toast implementation that supports 26.3, or make unsupported toast functionality safely optional.
3. Build against the 26.3 API using the required JDK and resolve any API changes.
4. Start a Spigot 26.3 server and test plugin loading, channel creation/join/leave, chat routing, menus, toast notifications, permissions, persistence/restart, and configured BungeeCord integration.

Keep dependency/API upgrades separate from a support claim until these checks pass. Existing developer-specific JAR output/copy paths in pom.xml also need cleanup before a portable install workflow can be relied on.

## Contributing

Open a pull request describing the change and the server versions actually tested.
