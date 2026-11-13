# Point-to-Point Portals (`mc-ppp`)

[![Minecraft](https://img.shields.io/badge/Minecraft-26.2%2B-brightgreen.svg)](https://www.minecraft.net/)
[![Fabric Loader](https://img.shields.io/badge/Fabric-0.19.5%2B-blue.svg)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://adoptium.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Point-to-Point Portals** is a Minecraft Fabric mod engineered to provide an immersive, balanced fast-travel mechanism deeply integrated with vanilla mechanics (Redstone automation and exploration).

Unlike traditional teleportation mods that rely on text menus or sprawling global networks, Point-to-Point Portals introduces physical blocks that form **strict, exclusive 1-to-1 pairs** (`Portal A <-> Portal B`). Each link acts as a dedicated bidirectional wormhole bridging distant coordinates or different dimensions, requiring continuous Redstone power to remain open.

---

## 🌟 Key Features

### 1. Dual-State Portal Block (`TeleporterBlock`)
Each portal block operates through three clear visual and mechanical states:
- **Inactive (Unlinked):** Freshly placed or unbound. The portal emits no light and does not teleport entities; POWERED still tracks Redstone.
- **Standby (Linked, Unpowered):** Bound to an active twin, but lacking Redstone activation. Displays faint, glowing rune indicators, but teleportation remains armed and closed.
- **Active (Linked & Powered):** Bound to an active twin **AND** supplied with a Redstone signal (`POWERED = true`). Fully opens with bright illumination, rune particles, and instant entity transport.

> **Link recovery:** Removal immediately unlinks a loaded reciprocal twin. If the twin is unloaded, reconciliation is deferred until both endpoints can be inspected. Periodic checks every 40 ticks do not load remote chunks; interactions and transport attempts can resolve them synchronously.

### 2. Exclusive 1-to-1 Linking Tool (`TunerItem` / Phase Tuner)
Pairing portals is intuitive and protected against accidental overwrites:
- **First Node Selection:** `Right-Click` on an unlinked portal to store its coordinates and dimension into the Phase Tuner using modern **Data Components**.
- **Circuit Closure:** `Right-Click` on a second unlinked portal to atomically create a mutual bidirectional link (`A <-> B`) and reset the tool.
- **Anti-Overwrite Protection:** Attempting to link a portal that is already bound is rejected with an in-game warning message.
- **Manual Disconnect:** With no stored selection, `Sneak + Right-Click` unlinks the clicked portal and its loaded reciprocal twin; an unloaded twin reconciles later. With a stored selection, the same gesture clears the tuner first.

### 3. Anti-Loop Teleportation Safeguards
To eliminate infinite ping-pong loops when arriving at a destination:
- Transported entities receive a 60-tick cooldown (3 seconds). Standing on the destination refreshes it to 40 ticks; step away and let it expire before returning.
- Activation is automatic when stepping onto a powered, linked pad.

### 4. Cross-Dimensional & Chunk Resilience
- Seamlessly bridges any vanilla or modded dimensions (`Overworld`, `The Nether`, `The End`).
- Periodic checks retain unloaded twins without loading them. Tuner interactions and transport resolve chunks synchronously; latency remains to be measured. See the [loading performance protocol](docs/loading-performance.md).

---

## 🛠️ Crafting Recipes

Progression is balanced around mid-game exploration (Nether travel and Amethyst Geodes):

### Teleporter Block (Produces 2 units)
Crafts a complete matching pair in one recipe:
- **Corners:** Obsidian (`minecraft:obsidian`)
- **Edges:** Copper Ingot (`minecraft:copper_ingot`) or Redstone Dust (`minecraft:redstone`)
- **Center:** Eye of Ender (`minecraft:ender_eye`)

```text
[ Obsidian ] [  Copper  ] [ Obsidian ]
[ Redstone ] [ EyeEnder ] [ Redstone ]
[ Obsidian ] [  Copper  ] [ Obsidian ]
```

### Phase Tuner (Linking Tool)
- **Top / Bottom:** Amethyst Shards (`minecraft:amethyst_shard`)
- **Center:** Ender Pearl (`minecraft:ender_pearl`)
- **Handle:** Blaze Rod (`minecraft:blaze_rod`)

```text
[      -      ] [ Amethyst Shard ] [      -      ]
[      -      ] [  Ender Pearl   ] [      -      ]
[      -      ] [   Blaze Rod    ] [      -      ]
```

---

## 🏗️ Technical Architecture & Standards

- **Target Platform:** Minecraft Java Edition 26.2+
- **Mod Loader:** Fabric Loader >= 0.19.5
- **Fabric API:** >= 0.159.0, using the build for Minecraft 26.2
- **Toolchain:** Gradle 9.8+, Fabric Loom 1.19+, Java 25; official Mojang mappings
- **Data Persistence:** Modern `DataComponentType` (strictly zero legacy NBT on ItemStacks).
- **Registries:** Built on standard Fabric API and Minecraft built-in registries.
- **Agent Governance:** Development follows the **Iterative Refinement Pattern** (`Plan -> Implement -> Verify -> Refine`). See [`AGENTS.md`](AGENTS.md).

---

## 🚀 Building & Developing

### Prerequisites
- **JDK 25** (e.g. [Eclipse Temurin 25 LTS](https://adoptium.net/)) configured in `JAVA_HOME`.
- Git.

Gradle uses a Java 25 toolchain. Set `JAVA_HOME` to a JDK 25 installation
before running the wrapper; Loom also requires Java 25 to run Gradle itself.
Keep machine-specific JDK paths in your user Gradle configuration, outside this repository.

The development baseline is Minecraft 26.2, Fabric Loader 0.19.5 and Fabric API
`0.159.0+26.2`. This combination passes the 35 server GameTests and starts and
stops a dedicated production server with the packaged mod. Earlier Loader
0.19.x versions and Minecraft versions before 26.2 are not supported by this
change. A real-client gameplay check remains separate from these automated
tests. The relaxed requirements are currently unreleased; previously published
0.0.10 and 0.0.9 JARs still require Loader >= 0.20.1.

### Common Gradle Commands

```bash
# Decompile and generate Minecraft source jars for IDE navigation
./gradlew genSources

# Compile and build the mod JAR
./gradlew build

# Run the server regression suite with optional JFR diagnostics
./gradlew runGameTest -PportalProfile

# Isolated automated loading benchmark (PowerShell; writes a fresh world/report)
./scripts/Run-LoadingBenchmark.ps1

# Run Fabric Data Generation pipeline (models, recipes, blockstates, lang)
./gradlew runDatagen

# Launch test client
./gradlew runClient

# Launch test dedicated server
./gradlew runServer
```

---

## Releases

The current project version is **0.0.10**. Preparing this version does not
publish a release; publication starts only after pushing its version tag.

Release automation runs only when a `v*` tag is pushed. Pushes and pull requests
on `devel` and `main` do not start Actions. Tests and benchmarks are not run by
the release workflow; run the ordinary `./gradlew build` locally on the final
release commit before tagging it. Benchmarks remain a separate, manual task.

1. Set `mod_version` and add matching release notes in `CHANGELOG.md`, then commit.
2. For a stable release, integrate that commit into `main` and verify it locally.
   For a release candidate, use a commit on `devel`.
3. Create and push the matching tag, for example `v0.0.10` or `v0.0.10-rc.1`.
   These are examples: the current version remains defined in `gradle.properties`.

The [release workflow](.github/workflows/release.yml) checks the exact tagged
commit, branch ancestry, version and changelog before installing Java 25.
It runs `bash ./gradlew --no-daemon jar sourcesJar`, verifies the resulting
packages and publishes a GitHub Release with the mod JAR, sources JAR and
`SHA256SUMS.txt`. Published files include the mod version, Minecraft version and
loader: `pointportals-0.0.10-mc26.2-fabric.jar` and
`pointportals-0.0.10-mc26.2-fabric-sources.jar`. These names are staged in
`build/release`; original Gradle outputs in `build/libs` are unchanged.
The tag remains plain SemVer with a `v` prefix, such as `v0.0.10`.
Only the runtime JAR requires expanded version metadata;
the sources JAR includes the original resource template. RCs are prereleases;
older stable versions do not replace a newer stable release as Latest.

One Ubuntu job uses SHA-pinned actions and the built-in `GITHUB_TOKEN` with
`contents: write`; no personal token or additional secrets are required.
Gradle's basic cache is enabled, but GitHub cache scope restricts reuse of
tag-created caches to that same tag, so a new version can still need fresh
dependency downloads. No intermediate Actions artifacts or benchmark worlds
are uploaded. Packaging does not attest that local tests were run.

Existing releases, including drafts, are never overwritten. If uploading fails,
inspect the draft and complete it manually or remove the incomplete draft
before rerunning the workflow; do not move the version tag. Failed runs can be
rerun from Actions once the cause has been addressed. The workflow must be
included in the tagged commit. Repository Actions permissions must allow its
write token. Local validation: `python -B scripts/test_release.py`.

## 📄 License
This project is licensed under the terms of the [MIT License](LICENSE).
