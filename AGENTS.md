# Point-to-Point Portals - Agent Constitution (`AGENTS.md`)

## 1. Project Overview & Architecture Vision
**Point-to-Point Portals** is a Minecraft Fabric mod (Minecraft Java Edition 26.2+, Java 25, Fabric API) engineered to provide an immersive, balanced fast-travel mechanism deeply integrated with vanilla mechanics (Redstone and exploration).

### Core Architectural Axioms
1. **Strict 1-to-1 Exclusive Pairing:**
   - Every portal block (`TeleporterBlock`) links exclusively to exactly one twin portal (`Portal A <-> Portal B`).
   - No networks, hubs, or 1-to-many / many-to-1 configurations are permitted.
   - If Portal A is linked to Portal B, Portal B's stored destination must be Portal A, and vice versa.
2. **Dual BlockState Mechanics (`LINKED` & `POWERED`):**
   - **Inactive (Unlinked):** `LINKED = false`, `POWERED = false/true`. The portal has no destination. Cannot transport entities. No light emitted.
   - **Standby (Linked, Unpowered):** `LINKED = true`, `POWERED = false`. Portal is bound to an active twin, but lacks Redstone activation. Emits faint light/indicator, but teleportation is disabled.
   - **Active (Linked & Powered):** `LINKED = true`, `POWERED = true`. Fully active with light emission, particles, and entity teleportation.
3. **Redstone Dependency:**
   - Portals require continuous Redstone power to maintain active status. Loss of Redstone power instantly transitions the portal back to Standby (`POWERED = false`).
4. **Chunk-Loading & Cross-Dimension Safety:**
   - Portals can bridge cross-dimensional destinations (Overworld, Nether, End, or custom dimensions via `RegistryKey<World>`).
   - Twin verification must handle unloaded destination chunks gracefully: verify target world existence, safely load/resolve the destination chunk or verify block state without causing memory leaks or server lag.
   - If the destination portal is broken or replaced, the source portal must cleanly revert to unlinked (`LINKED = false`).
5. **Anti-Loop Teleportation Safeguards:**
   - Entities arriving at a portal must never immediately trigger a return teleportation loop.
   - Enforce an entity teleportation cooldown (minimum 40 ticks / 2 seconds) or check sneak state / cooldown tags on the entity before initiating transport.

---

## 2. Technical Stack & Coding Standards
- **Target Platform:** Minecraft 26.2+ (Java Edition)
- **Mod Loader:** Fabric Loader (>= 0.20.1)
- **Toolchain:** Gradle 9.x with Fabric Loom 1.19+, Java 25 (`JavaCompile.release = 25`)
- **Mappings:** Official Mojang Mappings (built into modern Fabric Loom)
- **Base Package:** `com.project.pointportals`
- **Mod ID:** `pointportals`
- **Modern Item Data Persistence:**
   - **Data Components ONLY:** Modern Minecraft (1.20.5+ through 26.x+) uses `DataComponentType`. Legacy `ItemStack` NBT is deprecated/removed. All item metadata (such as `TunerItem` selected portal position and dimension) must strictly use custom registered `ComponentType<T>`.
- **BlockEntity Persistence:**
   - Custom `BlockEntity` persistence must use modern `writeNbt` and `readNbt` (or `saveAdditional`/`loadAdditional`) matching 26.x API conventions.
- **Event Handling & Registry:**
   - Use standard Fabric API registry hooks (`Registry.register(Registries.BLOCK, ...)`, `Registry.register(Registries.ITEM, ...)`, etc.).
   - Asset and data generation must leverage `FabricDataGenerator` entrypoint.

---

## 3. Mandatory Development Workflow: Iterative Refinement Pattern
Never generate large, monolithic chunks of code in a single turn. All modifications, features, and refactorings must adhere to the 4-phase loop:

```
  +--------+        +-----------+        +-------------------------+        +-----------------+
  | 1.Plan | -----> | 2.Implement| -----> | 3.Verify (gradlew build)| -----> | 4.Review & Refine|
  +--------+        +-----------+        +-------------------------+        +-----------------+
      ^                                                                               |
      +----------------------------- Next Iteration ----------------------------------+
```

1. **Plan:** Propose a focused, self-contained increment (target classes, JSON definitions, or build configs). Define expected behavior and validation criteria.
2. **Implement:** Write clean, modular, production-ready Java 21 code and valid JSON files. Ensure proper imports and Yarn 1.21.4 mapping usage.
3. **Verify:** Run `./gradlew build` (and `./gradlew runDatagen` when modifying assets/data). Inspect compilation logs, warnings, and mapping errors. Fix any failure immediately before proceeding.
4. **Review & Refine:** Summarize what was accomplished, ensure adherence to `PROJECT_DESIGN.md`, and obtain user alignment for the next increment.

---

## 4. Local Agent Roles & Responsibilities
The workspace contains dedicated local agent profiles in `.agents/agents/`:
- **`fabric-architect`**: Core Java gameplay logic, blockstates, BlockEntities, Data Components, Redstone interactions, and safe cross-dimensional teleportation.
- **`datagen-asset-specialist`**: Fabric Data Generation pipeline, blockstate models (all 4 combinations of `linked` and `powered`), item models, recipes, loot tables, and `en_us.json` localization.
- **`qa-build-verifier`**: Gradle compilation audit, mapping mismatch diagnosis, edge-case validation (twin desync, loop prevention, unlinked breakdown), and code hygiene.

Refer to `.agents/skills/` for execution SOPs:
- `iterative-fabric-feature`
- `fabric-datagen-pipeline`
- `portal-link-integrity-audit`
