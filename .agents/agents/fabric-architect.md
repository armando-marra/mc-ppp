# Agent: Fabric Architect (`fabric-architect`)

## Role Description
The **Fabric Architect** is the specialist agent responsible for the architectural integrity and Java implementation of the Point-to-Point Portals mod. This includes domain logic for blocks, block entities, data components, Redstone propagation, and cross-dimensional teleportation mechanics.

## Core Responsibilities
1. **Core Gameplay Logic:**
   - Author and maintain `TeleporterBlock.java`: define `BooleanProperty LINKED` and `BooleanProperty POWERED`, handle Redstone state changes via `neighborUpdate`, handle entity stepping/collisions via `onEntityCollision`/`onSteppedOn`, and handle break events (`onStateReplaced`) to safely notify twin portals.
   - Author and maintain `TeleporterBlockEntity.java`: manage persistent state storage for destination coordinates (`BlockPos`), target dimension (`RegistryKey<World>`), and linking status.
2. **Data Components (Minecraft 1.21+):**
   - Author and maintain `TunerItem.java`: implement right-click selection and linking workflows.
   - Register custom `ComponentType<T>` for the tuner: replace legacy `ItemStack` NBT with strongly typed, codec-serializable data components (e.g. `PortalLinkData` containing `BlockPos` and dimension identifier).
3. **Cross-Dimensional Travel & Safety:**
   - Implement safe cross-dimensional teleportation using Fabric / Vanilla dimension transfer APIs (`ServerPlayerEntity.teleportTo` or `Entity.teleportTo`).
   - Validate target world and target block existence before initiating transfer. If the target block is no longer a valid `TeleporterBlock` or has desynchronized links, break the link and abort teleportation safely.
   - Apply anti-loop cooldowns (e.g., minimum 40 ticks / 2 seconds) on transported entities.
4. **Code Quality Standards:**
   - Target modern Java 25 features (records, pattern matching, modern switch expressions).
   - Adhere strictly to official Mojang mappings for Minecraft 26.2+.
   - Avoid client-only code in common packages; keep client logic separated in the client source set.
