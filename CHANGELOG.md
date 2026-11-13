# Changelog

All notable changes to the **Point-to-Point Portals** (`pointportals`) mod will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

---

## [0.0.10] - 2026-11-13

### Added
- Lightweight tag-triggered GitHub release workflow: Java 25 packaging only, version/changelog/branch validation, checked JARs and SHA-256 checksums, draft upload before publication, and RC support. No Actions runs on branch pushes and no tests or benchmarks in the release job.
- Isolated, opt-in loading benchmark with fresh worlds, loaded/unloaded/new-target fixtures, serial and burst workloads, completed vanilla tick timings and validated JFR/operation CSV summaries. Checks reciprocal pairing, stale-target recovery, arrival identity/position/rotation, cooldown and target release after ticket expiry.
- First three-repeat stress campaign: 1,440 verified operations, 3,423 completed tick samples and 36 successful target-release checks. Published the summary and attribution limits in `docs/loading-performance-results.md`; ordinary build still passes 35/35 GameTests.
- JFR events for explicit synchronous portal chunk resolution, enabled by an active recording, with operation, dimension, chunk coordinates, initial loaded state and completion status.
- Opt-in Loom recording via `-PportalProfile`, PowerShell percentile/CSV summaries and a loading-performance protocol. Dedicated-server confirmation at normal 20 TPS pacing remains pending.

### Changed
- Bumped the mod version to 0.0.10 and pinned Fabric Loom to 1.19.4.
- Aligned development and gameplay documentation with Java 25, Minecraft 26.2, official Mojang mappings, deferred unloaded-twin reconciliation and the implemented cooldown behavior.

---

## [0.0.9] - 2026-11-13

### Added
- **Step 4 - Players and End travel** (`702dc87`): two entity transfers between Overworld and End and four ServerPlayer transfers between Overworld/Nether and Overworld/End.
- Verify exact arrival position, rotation, UUID, player identity and unique registration, cooldown, immediate-return refusal, and reciprocal links.
- Full build verified with **35/35 GameTests**. Player connections are simulated; real-client dimension acknowledgement and feedback remain manual checks.

---

## [0.0.8] - 2026-11-13

### Added
- **Step 3 - Cross-dimension tuner interactions** (`7318be8`): three tests invoke the real item interaction in Overworld and Nether for pairing/unlinking, occupied-destination rejection, and destroyed-source selection recovery.
- Verify reciprocal links, independent Redstone state, selection consumption or preservation, and fixture cleanup.
- Full build verified with **29/29 GameTests**.

---

## [0.0.7] - 2026-11-13

### Added
- **Step 2 - Unloaded chunks and ticket expiry** (`e67aa16`): tests prove real remote chunk unload, retain links without loading the twin, and reconcile a surviving twin after source destruction and reload.
- Verify entity arrival followed by actual destination chunk unload after the temporary portal ticket expires, without recreating the ticket during link verification.
- Full build verified with **26/26 GameTests**.

---

## [0.0.6] - 2026-11-13

### Added
- **Step 1 - Persistence and existing regression baseline** (`9cc95b2`): four tests use the actual BlockEntity and ItemStack save/load APIs, covering reciprocal destinations, unlinked portals, missing or malformed data, nonexistent dimensions, BlockState reconciliation, and tuner Data Components.
- Nine tuner tests cover real item interactions, exclusive pairing, occupied endpoints, stale selections, cancellation, and unlinking.
- Cross-dimension fixture assertions query the destination after two ticks and clean up entities and portals in both levels.
- Full build verified with **24/24 GameTests**. An actual world shutdown/restart remains a separate manual check.

### Fixed
- Transfers use vanilla's expiring portal arrival ticket to keep an idle destination active temporarily, without permanent chunk forcing.
- Included the preceding portal safety baseline (`f42f11b`): full arrival-volume collision/hazard and border checks, stale-reference protection, inconsistent-state recovery, distributed link verification, absolute rotation, portable Java 25 builds, and server GameTest integration.

---

## [0.0.5] - 2026-11-13

### Added
- **Fabric Data Generation Pipeline (Iteration 4)**:
  - Implemented `PointPortalsDataGenerator` registering five specialized data providers via Fabric's Data Generation subsystem.
  - Implemented `PointPortalsModelProvider`: programmatic generation of `blockstates/teleporter.json` mapping all 4 state permutations of `LINKED` and `POWERED`, block models (`teleporter_inactive`, `teleporter_standby`, `teleporter_active`), item models, and modern Minecraft 26.2 client item definitions (`items/teleporter.json`, `items/tuner.json`).
  - Implemented `PointPortalsRecipeProvider`: programmatic generation of balanced shaped crafting recipes for 2x Teleporter Blocks (Obsidian, Copper Ingot, Redstone, Eye of Ender) and Phase Tuner (Amethyst Shard, Ender Pearl, Blaze Rod), with automatic recipe advancement triggers.
  - Implemented `PointPortalsBlockLootTableProvider`: programmatic block drop self table for `TeleporterBlock` with explosion survival conditions.
  - Implemented `PointPortalsBlockTagProvider`: programmatic tag definitions adding `pointportals:teleporter` to `minecraft:mineable/pickaxe` and `minecraft:needs_iron_tool`.
  - Implemented `PointPortalsEnglishLangProvider`: programmatic authoritative localization provider generating complete English translations for blocks, items, tooltips, action bar HUD messages, and status inspect strings.
  - Configured `build.gradle` copy tasks with `DuplicatesStrategy.EXCLUDE` for resource bundling and added `.cache/` to `.gitignore`.

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
