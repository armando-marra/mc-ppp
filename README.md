# Point-to-Point Portals (`mc-ppp`)

[![Minecraft](https://img.shields.io/badge/Minecraft-26.2%2B-brightgreen.svg)](https://www.minecraft.net/)
[![Fabric Loader](https://img.shields.io/badge/Fabric-0.20.1%2B-blue.svg)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://adoptium.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Point-to-Point Portals** is a Minecraft Fabric mod engineered to provide an immersive, balanced fast-travel mechanism deeply integrated with vanilla mechanics (Redstone automation and exploration).

Unlike traditional teleportation mods that rely on text menus or sprawling global networks, Point-to-Point Portals introduces physical blocks that form **strict, exclusive 1-to-1 pairs** (`Portal A <-> Portal B`). Each link acts as a dedicated bidirectional wormhole bridging distant coordinates or different dimensions, requiring continuous Redstone power to remain open.

---

## 🌟 Key Features

### 1. Dual-State Portal Block (`TeleporterBlock`)
Each portal block operates through three clear visual and mechanical states:
- **Inactive (Unlinked):** Freshly placed or unbound. The portal emits no light, ignores Redstone, and does not teleport entities.
- **Standby (Linked, Unpowered):** Bound to an active twin, but lacking Redstone activation. Displays faint, glowing rune indicators, but teleportation remains armed and closed.
- **Active (Linked & Powered):** Bound to an active twin **AND** supplied with a Redstone signal (`POWERED = true`). Fully opens with bright illumination, rune particles, and instant entity transport.

> **Safety Guarantee:** If either portal in a pair is broken or removed, its twin immediately detects the breakage and drops back to the *Inactive* state, preventing broken or corrupted links.

### 2. Exclusive 1-to-1 Linking Tool (`TunerItem` / Phase Tuner)
Pairing portals is intuitive and protected against accidental overwrites:
- **First Node Selection:** `Right-Click` on an unlinked portal to store its coordinates and dimension into the Phase Tuner using modern **Data Components**.
- **Circuit Closure:** `Right-Click` on a second unlinked portal to atomically create a mutual bidirectional link (`A <-> B`) and reset the tool.
- **Anti-Overwrite Protection:** Attempting to link a portal that is already bound is rejected with an in-game warning message.
- **Manual Disconnect:** `Sneak + Right-Click` on a linked portal immediately dissolves the link on both ends.

### 3. Anti-Loop Teleportation Safeguards
To eliminate infinite ping-pong loops when arriving at a destination:
- Transported entities receive an immediate teleportation cooldown (minimum 40 ticks / 2 seconds).
- Teleportation can also be configured to trigger upon sneaking on the active portal pad.

### 4. Cross-Dimensional & Chunk Resilience
- Seamlessly bridges any vanilla or modded dimensions (`Overworld`, `The Nether`, `The End`).
- Gracefully handles unloaded chunks without thread freezes, memory leaks, or chunk-loading deadlocks.

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
- **Mod Loader:** Fabric Loader >= 0.20.1
- **Toolchain:** Gradle 9.8+, Fabric Loom 1.19+, Java 25
- **Data Persistence:** Modern `DataComponentType` (strictly zero legacy NBT on ItemStacks).
- **Registries:** Built on standard Fabric API and Minecraft built-in registries.
- **Agent Governance:** Managed by local Antigravity AI agents following the **Iterative Refinement Pattern** (`Plan -> Implement -> Verify -> Refine`). See [`AGENTS.md`](AGENTS.md).

---

## 🚀 Building & Developing

### Prerequisites
- **JDK 25** (e.g. [Eclipse Temurin 25 LTS](https://adoptium.net/)) configured in `JAVA_HOME`.
- Git.

### Common Gradle Commands

```bash
# Decompile and generate Minecraft source jars for IDE navigation
./gradlew genSources

# Compile and build the mod JAR
./gradlew build

# Run Fabric Data Generation pipeline (models, recipes, blockstates, lang)
./gradlew runDatagen

# Launch test client
./gradlew runClient

# Launch test dedicated server
./gradlew runServer
```

---

## 📄 License
This project is licensed under the terms of the [MIT License](LICENSE).
