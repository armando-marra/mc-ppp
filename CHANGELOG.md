# Changelog

All notable changes to the **Point-to-Point Portals** (`pointportals`) mod will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned
- **Iteration 4**:
  - Fabric Data Generation pipeline for blockstates, models, recipes, loot tables, and `en_us.json`.

---

## [0.0.4] - 2026-11-12

### Added
- **Cross-Dimensional Teleportation & Anti-Loop Safeguards (Iteration 3)**:
  - Implemented cross-dimensional teleportation via `entity.teleportTo` in `TeleporterBlockEntity`.
  - Implemented anti-loop ping-pong protection using native `portalCooldown`: applies 60 ticks (3 seconds) cooldown on departure, and continuously resets cooldown to 40 ticks while standing on the destination teleporter, preventing unwanted return teleportation until the entity steps off.
  - Implemented immediate stepping activation via `TeleporterBlock.stepOn` and continuous standing detection via `serverTick` with AABB bounding box collision.
  - Added obstruction and hazard verification: verifies destination `standPos` and `headPos` for full-block collisions, `Blocks.LAVA`, `Blocks.FIRE`, and `Blocks.SOUL_FIRE`.
  - Added audio and visual effects: `SoundEvents.ENDERMAN_TELEPORT` and portal particles on departure/arrival, with `SoundEvents.DISPENSER_FAIL` and action-bar notification when obstructed.
  - Added passenger and vehicle safeguards: disallows teleportation for riding passengers or vehicles with riders to prevent entity desynchronization across dimensions.
  - Audited and certified by dedicated `qa-build-verifier` subagent with full scorecard approval.

---

## [0.0.3] - 2026-11-12

### Added
- **BlockEntity & Synchronization Mechanics (Iteration 2)**:
  - Registered `pointportals:teleporter` (`BlockEntityType<TeleporterBlockEntity>`) in `ModBlockEntities`.
  - Implemented `TeleporterBlockEntity` with modern `ValueInput` and `ValueOutput` NBT persistence and `ClientboundBlockEntityDataPacket` client synchronization.
  - Implemented chunk-unload safe destruction lifecycle: replaced generic `setRemoved()` hook with `preRemoveSideEffects()`, ensuring chunk unloading never unlinks twin portals.
  - Implemented self-healing bidirectional link verification (`verifyAndSyncLink()`) ensuring strict 1-to-1 reciprocity and automatic unlinking if a twin portal is destroyed, replaced, or desynchronized.
  - Added periodic server tick (`serverTick`) on `TeleporterBlock` every 40 ticks (2 seconds) to audit link integrity.
  - Implemented dynamic Redstone power detection (`neighborChanged` & `onPlace`) with audio feedback (`beacon.activate` / `beacon.deactivate`).
  - Added Redstone Comparator analog output signal support: `0` when unlinked, `7` when in standby, and `15` when fully powered/active.
  - Added client-side visual particles and ambient sound loop (`animateTick`) displaying rising portal particles when active and faint enchantment glimmers in standby.
  - Added right-click teleporter status inspection (`useWithoutItem`) providing action-bar feedback on pairing status and target coordinates.
  - Enhanced `TunerItem` to safely load target chunk on pairing completion, resolving link failures for distant or cross-dimension portals.
  - Added English localization keys for portal status messages in `en_us.json`.

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
