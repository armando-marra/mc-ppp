# Crafting images

The two PNGs are composed from the Minecraft 26.2 textures in the local client
JAR and the mod's generated recipe/item/block model JSON. They are recipe
illustrations, not screenshots. Minecraft textures originate from Mojang's game
assets; they are not original artwork covered by this repository's code license.
The Teleporter is shown with its inactive item model and the approved original
copper-and-obsidian textures from `src/main/resources/assets/pointportals/textures/block/`.
The Phase Tuner uses the
mod's original transparent 32×32 item texture at
`src/main/resources/assets/pointportals/textures/item/tuner.png`. Its generated
artwork source is kept in `docs/artwork/phase-tuner-source.png`; the runtime
sprite is reduced with nearest-neighbor sampling to preserve pixel edges.

To regenerate, install Pillow in your Python environment, run `./gradlew runDatagen`
if recipes/models changed, then from the repository root run:

```powershell
python scripts/render_readme_recipes.py --minecraft-jar "PATH/TO/minecraft-client.jar"
```

Fabric Loom's local cache contains the client JAR after development setup.
The script only reads that JAR; do not add the JAR to this repository.
