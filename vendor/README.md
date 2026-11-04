# Bundled Maven artifacts

This directory preserves the two JARs previously installed manually in the developer's Maven cache. The root POM uses `vendor/maven` as a file repository, so a fresh checkout can resolve them through the normal Maven build. The binaries are copied byte-for-byte from the project's existing local `libs` directory. SHA-1 and SHA-256 sidecars cover each JAR and its standalone POM, and Maven requires a matching checksum when it downloads them from this repository.

| Maven coordinates | Original local filename | SHA-256 |
| --- | --- | --- |
| `com.frengor:toastapi:1.0.1` | `ToastAPI-1.0.1.jar` | `f942027bbf56bfdb1db56062b24cb3be8a3281f2e5b9cf65b6e5ca62f0a1f8d1` |
| `com.github.stefvanschie.inventoryframework:IF-Lite:0.11.1` | `IF-Lite-0.11.1.jar` | `eb96291df21869ccaa1c78b4de6c014740310a940a2715c507cd21b521b6e141` |

ToastAPI's `plugin.yml` identifies version `1.0.1` and author `Perotin`. Its embedded Maven metadata identifies `com.frengor:toastapi:1.0.0`. The original source revision for this local build is unknown. The JAR includes `LICENSE`, `LGPL` and `NOTICE`, which retain the upstream copyright and LGPL v3-or-later notice. The standalone POM here describes the existing binary without requiring the private parent POM that the old local installation referenced.

IF-Lite's embedded metadata identifies InventoryFramework `IF` version `0.11.1`. The original instructions used to produce this local Lite build are unknown. This repository preserves the exact binary already used by PlayerChannels. InventoryFramework is released under the Unlicense. [Upstream project](https://github.com/stefvanschie/IF) · [Upstream license](https://github.com/stefvanschie/IF/blob/master/LICENSE)

The standalone POMs have no additional dependencies because these local JARs were already used as bundled artifacts, and the root project supplies the Spigot API. Keep the existing JAR contents, including their license entries, when shading them into PlayerChannels. Future library replacements should use a new version and be reviewed for API and server compatibility.
