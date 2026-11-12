---
name: fabric-datagen-pipeline
description: Instructions and best practices for configuring and running Fabric's Data Generation subsystem to produce blockstates, models, recipes, loot tables, and language assets.
---

# Fabric Data Generation Pipeline SOP

This skill outlines how to build and maintain programmatic asset generation for the Point-to-Point Portals mod using Fabric Data Generation.

## Setup Requirements
1. **Entrypoint:** `PointPortalsDataGenerator` implementing `net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint`.
2. **Configuration in `fabric.mod.json`:**
   ```json
   "entrypoints": {
     "fabric-datagen": [
       "com.project.pointportals.datagen.PointPortalsDataGenerator"
     ]
   }
   ```
3. **Gradle Configuration in `build.gradle`:**
   Ensure the `loom.runs.datagen` block is configured with `-Dfabric-api.datagen` and output directory targeting `src/main/generated`.

## Data Providers Overview
- **Model Generation (`FabricModelProvider`):**
  - Blockstate definitions: Map all four combinations of `LINKED` and `POWERED` to appropriate block models.
  - Item models: Generate standard flat item models or custom block item parent models.
- **Recipe Generation (`FabricRecipeProvider`):**
  - Implement shaped and shapeless crafting recipes:
    - 2 Teleporter Blocks (Obsidian corners, Copper/Redstone edges, Eye of Ender center).
    - Phase Tuner (Amethyst shards top/bottom, Ender Pearl center, Blaze Rod handle).
- **Loot Tables (`FabricBlockLootTableProvider`):**
  - Configure self-drop for `TeleporterBlock` (with silk touch or standard pickaxe drop).
- **Translations (`FabricLanguageProvider`):**
  - Maintain `en_us.json` strings for block and item names, UI messages, tooltips, and subtitles.
- **Block Tags (`FabricTagProvider.BlockTagProvider`):**
  - Assign `BlockTags.PICKAXE_MINEABLE` and `BlockTags.NEEDS_IRON_TOOL` to `TeleporterBlock`.

## Execution Workflow
1. Run datagen command:
   ```powershell
   ./gradlew runDatagen
   ```
2. Verify output in `src/main/generated/`:
   - Inspect generated JSON files under `assets/pointportals/` and `data/pointportals/`.
   - Ensure no corrupt JSON or unmapped identifiers exist.
3. Include generated resources in `build.gradle` source sets so that `./gradlew build` bundles them into the final JAR.
