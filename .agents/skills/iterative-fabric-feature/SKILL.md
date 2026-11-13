---
name: iterative-fabric-feature
description: Standard Operating Procedure for introducing a new Fabric block, item, block entity, or gameplay mechanic using an iterative build-and-verify loop.
---

# Iterative Fabric Feature Development SOP

This skill defines the mandatory protocol for adding new features (blocks, items, block entities, or mechanics) to the Point-to-Point Portals codebase.

## Workflow Phases

### Phase 1: Target Definition & Planning
1. **Identify the Increment:** Restrict changes to one cohesive logical unit (e.g., Block & Item registration, BlockEntity state, or Data Component definition).
2. **List Target Files:** Explicitly identify target Java classes, package locations, and any configuration entries required in `fabric.mod.json`.
3. **Verify API Signatures:** Confirm official Mojang mapping signatures for Minecraft classes (e.g., `BlockPos`, `BlockState`, `BlockEntity`, `DataComponentType`, `InteractionResult`).

### Phase 2: Implementation
1. Follow modern Java 25 conventions (clean records, pattern matching, explicit nullability checks where appropriate).
2. Use modern Minecraft 26.2+ patterns:
   - Use `DataComponentType` rather than deprecated NBT on `ItemStack`.
   - Implement `BlockEntity` serialization using modern save/load conventions.
   - Register registry objects via `Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name), block)`.
3. Separate client logic from common logic to ensure clean dedicated server execution.

### Phase 3: Verification with Gradle
1. Run compilation check via terminal:
   ```powershell
   ./gradlew compileJava
   ```
2. Run full mod check and remapping:
   ```powershell
   ./gradlew build -x test
   ```
3. Inspect output:
   - If compile errors or mapping mismatches occur, review line and column numbers.
   - Cross-reference official Mojang mappings for Minecraft 26.2 and fix issues immediately.
   - Never proceed to the next milestone until the current build is completely green.

### Phase 4: Review and Alignment
1. Summarize the completed changes.
2. Confirm alignment against `PROJECT_DESIGN.md`.
3. Document any follow-ups or edge cases to be addressed in subsequent iterations.
