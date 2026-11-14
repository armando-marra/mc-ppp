# Teleporter proposal: copper and obsidian

This design was approved and installed as the Teleporter's runtime textures.
`preview.png` projects the actual 32×32 face
textures onto cubes; it is not an in-game screenshot and does not simulate emitted
light or bloom.

`source-atlas.png` was generated with imagegen for this project. It has three
columns (inactive, standby, active) and three rows (top, side, bottom). Tiles were
extracted and reduced with nearest-neighbor sampling into `textures/`.

The design combines a copper frame, patinated corner bolts and an obsidian body.
An unlinked portal has dark runes; standby uses small muted cyan indicators;
active uses a bright cyan/violet center and side circuits. The bottom is an
obsidian foundation with copper bracing. No extra mechanic is implied.

Regenerate the preview with Pillow installed:

```powershell
python scripts/render_teleporter_proposal.py
```

The runtime copies are in `src/main/resources/assets/pointportals/textures/block/`.
The Fabric model provider selects the top, side and bottom for each block state.
The inventory item uses the inactive model. The four `linked`/`powered` variants
keep their existing mapping to the three visual states.
