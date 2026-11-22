# Bundled Maven artifacts

The file repository under `vendor/maven` supplies the following libraries for PlayerChannels. Each JAR and POM has SHA-1 and SHA-256 sidecars.

| Maven coordinates | Library JAR SHA-256 |
| --- | --- |
| `com.github.stefvanschie.inventoryframework:IF-Lite:0.12.2` | `d730e41a1728057aaf4e9fe9a772e09d54401cc4f80e10df6625007fbd3e4916` |
| `com.frengor:toastapi:2.0` | `b1850bca50e77b1bdadf8c6a26b269051ffaed294594dc03cc965c97f3d575df` |

**IF-Lite 0.12.2** packages the shared classes for PlayerChannels chest menus and paging from [upstream IF 0.12.2](https://github.com/stefvanschie/IF/releases/tag/v0.12.2). The Maven Central IF JAR has SHA-256 `4af4d21cbbbe1ae97c700b2f7ab69b3c1301d0c719481080af829d342d5dcedd`. The Lite build omits version-specific NMS inventories and font resources, and preserves both inventory-view implementations. Its Unlicense is embedded at `META-INF/IF-Lite/LICENSE`. To reproduce the JAR, check out [the IF-Lite source at d0f2488](https://github.com/maximus-f/IF-Lite/tree/d0f2488826e834b1c492033e96788dbe3c8c417e) and run `mvn clean verify` with Maven 3.9 and JDK 17.

**ToastAPI 2.0** is the shadeable `ToastAPI/target/toastapi-2.0.jar` built from [upstream revision f8e6f72](https://github.com/maximus-f/ToastsAPI/tree/f8e6f72e8cb5b0d7bd9b90a042d94ea4c568a038). Its standalone POM describes the already-bundled library. The JAR contains its adapters and license files.

## Historical artifacts

The 0.11.1 IF-Lite and 1.0.1 ToastAPI directories preserve the binaries previously installed manually in the developer's Maven cache. The root POM uses `vendor/maven` as a file repository, so a fresh checkout can resolve them through the normal Maven build. The binaries are copied byte-for-byte from the project's existing local `libs` directory. SHA-1 and SHA-256 sidecars cover each JAR and its standalone POM, and Maven requires a matching checksum when it downloads them from this repository.

| Maven coordinates | Original local filename | SHA-256 |
| --- | --- | --- |
| `com.frengor:toastapi:1.0.1` | `ToastAPI-1.0.1.jar` | `f942027bbf56bfdb1db56062b24cb3be8a3281f2e5b9cf65b6e5ca62f0a1f8d1` |
| `com.github.stefvanschie.inventoryframework:IF-Lite:0.11.1` | `IF-Lite-0.11.1.jar` | `eb96291df21869ccaa1c78b4de6c014740310a940a2715c507cd21b521b6e141` |

ToastAPI's `plugin.yml` identifies version `1.0.1` and author `Perotin`. Its embedded Maven metadata identifies `com.frengor:toastapi:1.0.0`. The original source revision for this local build is unknown. The JAR includes `LICENSE`, `LGPL` and `NOTICE`, which retain the upstream copyright and LGPL v3-or-later notice. The standalone POM here describes the existing binary without requiring the private parent POM that the old local installation referenced.

IF-Lite's embedded metadata identifies InventoryFramework `IF` version `0.11.1`. The original instructions used to produce this local Lite build are unknown. This repository preserves the exact binary already used by PlayerChannels. InventoryFramework is released under the Unlicense. [Upstream project](https://github.com/stefvanschie/IF) · [Upstream license](https://github.com/stefvanschie/IF/blob/master/LICENSE)

The standalone POMs have no additional dependencies because these local JARs were already used as bundled artifacts, and the root project supplies the Spigot API. Keep the existing JAR contents, including their license entries, when shading them into PlayerChannels. Future library replacements should use a new version and be reviewed for API and server compatibility.
