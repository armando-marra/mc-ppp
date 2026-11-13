---
name: portal-link-integrity-audit
description: Quality assurance checklist and audit procedure for testing 1-to-1 portal link consistency, Redstone synchronization, anti-loop cooldowns, and cross-dimension stability.
---

# Portal Link Integrity Audit SOP

This skill defines the verification checklist and audit procedures to validate the gameplay mechanics and edge-case handling of Point-to-Point Portals.

## Verification Checklist

### 1. 1-to-1 Exclusivity & Linking Mechanics
- [ ] **First Node Capture:** Clicking an unlinked portal with `TunerItem` records its `BlockPos` and `ResourceKey<Level>`. Action bar confirms selection.
- [ ] **Second Node Binding:** Clicking a second unlinked portal creates an atomic, bidirectional link (`Portal A -> Portal B` AND `Portal B -> Portal A`).
- [ ] **Self-Link Prevention:** Clicking the exact same portal twice with the tuner is rejected with an error message.
- [ ] **Occupied Target Protection:** Clicking an already linked portal with a primed tuner is rejected with an error message; no existing links are overwritten.
- [ ] **Manual Disconnect (`Shift + Right Click`):** Using an empty tuner with sneak unlinks the clicked portal and its loaded reciprocal twin; an unloaded twin reconciles later. With a stored selection, sneak clears selection first.

### 2. Redstone & BlockState Sync
- [ ] **Unlinked State:** `LINKED = false`, `POWERED = any` -> Portal does not trigger teleportation.
- [ ] **Standby State:** `LINKED = true`, `POWERED = false` -> Emits low light, teleporter is armed but dormant.
- [ ] **Active State:** `LINKED = true`, `POWERED = true` -> Receives Redstone power, activates particle/visual effects, allows teleportation.
- [ ] **Power Cut:** Removing the Redstone signal immediately deactivates teleportation.

### 3. Destruction & Twin Cleanup
- [ ] **Source Broken:** Breaking Portal A immediately causes Portal B to reset to `LINKED = false` if Portal B's chunk is loaded, or after deferred reconciliation once both endpoints can be inspected; periodic ticks must not force remote chunks to load.
- [ ] **Unloaded Chunk Handling:** If Portal B is in an unloaded chunk or another dimension, breaking Portal A must not crash the server or corrupt world save files.

### 4. Teleportation & Anti-Loop Safety
- [ ] **Arrival Anti-Loop Cooldown:** When an entity steps onto Portal A and is transferred to Portal B, Portal B must not immediately teleport the entity back to Portal A.
- [ ] **Entity Transfer:** Teleportation accurately preserves entity rotation/velocity or places the entity safely centered above Portal B.
- [ ] **Cross-Dimension Travel:** Transporting between Overworld, Nether, and End functions reliably with correct chunk loading and zero entity duplication.
