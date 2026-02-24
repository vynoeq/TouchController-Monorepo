# rules_mc

Bazel rules to download Minecraft.

Features:

- Download Minecraft client & server JARs and mappings
- Download Minecraft client assets (both legacy and new style)
- Download Minecraft libraries from official repository
- Handle platform constraints and native library extracting

## Usage

Insert following template into your MODULE.bazel:

```starlark
bazel_dep(name = "rules_mc", version = "0.0.1")
archive_override(
    module_name = "rules_mc",
    integrity = "<Insert hash from releases>",
    url = "<Insert URL from releases>",
)
```

And declare Minecraft versions you want in MODULE.bazel:

```starlark
minecraft = use_extension("@rules_mc//:extensions.bzl", "minecraft")

minecraft.version(
    # Specify the game version
    version = "1.21.8",
    # Download client assets
    assets = True,
    # Download client/server JARs and mappings
    client = True,
    client_mappings = True,
    server = True,
    server_mappings = True,
)

# You may need it to exclude libraries from downloading from Mojang.
# For example, Fabric loader refuse to load when there are two ASM libraries on classpath (one comes from rules_mc, another comes from rules_jvm_external or something like it).
minecraft.exclude_library(names = [
    "org.ow2.asm:asm:9.6",
])

use_repo(minecraft, "minecraft")
use_repo(minecraft, "minecraft_assets")
```

Once you set up, refer Minecraft artifacts using these labels:

```text
@minecraft//<version>:client
@minecraft//<version>:server
@minecraft//<version>:client_libraries
@minecraft//<version>:client_mappings
@minecraft//<version>:server_mappings
@minecraft_assets//:assets_<version>

# All assets, useful to being passed to client as runtime dependencies
@minecraft_assets//:assets
```
