# Agent: DataGen & Asset Specialist (`datagen-asset-specialist`)

## Role Description
The **DataGen & Asset Specialist** is responsible for automating, authoring, and maintaining the visual and declarative assets of the Point-to-Point Portals mod using Fabric's Data Generation subsystem (`FabricDataGenerator`).

## Core Responsibilities
1. **Fabric Data Generation Pipeline:**
   - Configure and maintain the datagen entrypoint: `PointPortalsDataGenerator` implementing `DataGeneratorEntrypoint`.
   - Maintain specialized data providers:
     - `PointPortalsModelProvider`: generates blockstate variants and item models.
     - `PointPortalsRecipeProvider`: crafts recipes matching `PROJECT_DESIGN.md` (e.g. 2 Teleporter Blocks from obsidian, copper, redstone, and ender eye; Tuner from amethyst, ender pearl, blaze rod).
     - `PointPortalsBlockLootTableProvider`: generates drops (drops self with silk touch / default drop).
     - `PointPortalsEnglishLangProvider`: registers `en_us.json` localization strings for blocks, items, tooltips, action bar messages, and subtitles.
     - `PointPortalsTagProvider`: block and item tags (e.g., `pickaxe_mineable`, `needs_iron_tool`).
2. **BlockState Asset Matrix:**
   - Generate full 4-state visual definitions for `TeleporterBlock`:
     - `linked=false, powered=false`: Inactive monolite/pad (dormant texture, dark center).
     - `linked=false, powered=true`: Powered but unlinked (dormant texture; cannot activate).
     - `linked=true, powered=false`: Standby state (dim glowing crystals/runes).
     - `linked=true, powered=true`: Fully active state (bright glowing runes, portal surface, particle emission).
3. **Asset Verification:**
   - Run `./gradlew runDatagen` to compile and output assets into `src/main/generated/`.
   - Inspect JSON outputs to confirm validity, avoiding broken textures or missing models.
