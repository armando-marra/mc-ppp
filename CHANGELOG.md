# Changelog

All notable changes to the **Point-to-Point Portals** (`pointportals`) mod will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned
- **Iteration 2**:
  - `TeleporterBlockEntity` implementation with persistent NBT serialization.
  - Safe Redstone power detection (`neighborChanged`) and synchronized twin unlinking.
- **Iteration 3**:
  - Cross-dimensional teleportation mechanics and anti-loop arrival cooldowns.
- **Iteration 4**:
  - Fabric Data Generation pipeline for blockstates, models, recipes, loot tables, and `en_us.json`.

---

## [0.0.2] - 2026-11-12

### Added
- **Core Registries & Data Components (Iteration 1)**:
  - Registered `pointportals:portal_target` (`DataComponentType<PortalTarget>`) storing dimension `ResourceKey<Level>` and `BlockPos` with full Codec and StreamCodec serialization.
  - Implemented `PortalTarget` implementing `TooltipProvider` for dynamic item inspection.
  - Registered `pointportals:teleporter` (`TeleporterBlock`) with `LINKED` and `POWERED` blockstates and dynamic light emission (0, 3, or 12).
  - Registered `pointportals:tuner` (`TunerItem`) featuring first-node capture, self-link rejection, shift-right-click memory clearing, visual foil enchantment glow when armed, and action-bar feedback.
  - Registered `BlockItem` for the teleporter with block description prefix support.
  - Added mod blocks and items to vanilla `FUNCTIONAL_BLOCKS` and `TOOLS_AND_UTILITIES` Creative Tabs via Fabric `CreativeModeTabEvents`.
  - Added full English in-game localization entries in `en_us.json`.

---

## [0.0.1] - 2026-11-12

### Added
- **Project Scaffolding**:
  - Initialized Fabric mod environment targeting Minecraft 26.2+ on Java 25.
  - Configured Gradle 9.8 toolchain with Fabric Loom 1.19.4 and Fabric API `0.161.0+26.2`.
  - Configured official Mojang mappings and split client/common source sets.
  - Established base package structure under `com.project.pointportals`.
- **Mod Entrypoints**:
  - Common entrypoint: `PointPortals` (`ModInitializer`).
  - Client entrypoint: `PointPortalsClient` (`ClientModInitializer`).
  - DataGen entrypoint: `PointPortalsDataGenerator` (`DataGeneratorEntrypoint`).
  - Fabric mod metadata (`fabric.mod.json`) with mod ID `pointportals`.
- **Local Agentic Governance**:
  - Root agent constitution (`AGENTS.md`) outlining architectural axioms, technical standards, and the 4-phase Iterative Refinement Pattern.
  - Local sub-agent profiles: `fabric-architect`, `datagen-asset-specialist`, and `qa-build-verifier`.
  - Local execution skills: `iterative-fabric-feature`, `fabric-datagen-pipeline`, and `portal-link-integrity-audit`.
- **Documentation & VCS**:
  - English `README.md` detailing gameplay design, mechanics, recipes, and build instructions.
  - Git version control initialization, remote GitHub repository setup, and MIT License.
