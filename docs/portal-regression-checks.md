# Portal regression checks

## Automated server tests

Run `./gradlew build` to compile the mod and execute the server GameTests.
Run `./gradlew runGameTest` to run just the gameplay suite. Tests live in
`src/gametest/java` and are excluded from the distributed mod JAR. The test
server uses a separate generated world in Loom's run directory.

The suite checks reciprocal links, stale-reference removal, twin destruction,
missing-destination recovery, partial collision shapes, successful transfer,
immediate-return cooldown, and loss of Redstone power. Cross-dimension tests
cover Overworld-to-Nether and Nether-to-Overworld travel with exact arrival
coordinates, UUID preservation, absence of a duplicate at the source, rotation,
arrival cooldown, reciprocal links, and refusal of an obstructed Nether exit.
Arrival assertions run two ticks after transfer, with both portals kept intact
until verification and fixture cleanup on success or assertion failure. Transfers
use vanilla's expiring portal ticket to activate an otherwise idle destination;
loading its blocks alone does not keep the arriving entity accessible.
The same arrival checks also cover pigs travelling Overworld-to-End and
End-to-Overworld, and registered mock ServerPlayers travelling both directions
between Overworld/Nether and Overworld/End. Player tests additionally verify
object identity, destination level and exactly one registration in the server
player list. Their embedded connection does not establish real-client UI or
dimension-change acknowledgement correctness; check those manually.

Persistence tests exercise the actual BlockEntity save/load APIs (including
full metadata reconstruction), reciprocal destinations, unlinked portals,
missing/malformed data, missing dimensions, blockstate reconciliation, and
the full tuner ItemStack codec with its selected-target component. They do
not restart the server; the save-and-reopen check below remains necessary.

Tuner tests invoke the actual item interaction with a mock survival player.
They check component selection, pairing and power state, self-link refusal,
occupied endpoints, a source claimed by a second tuner, a destroyed source,
selection cancellation, manual unlinking, and interaction with ordinary blocks.
Three further tuner tests exercise cross-dimension pairing/unlinking, occupied
destination rejection, and destroyed-source selection recovery using actual
item interactions in both levels. Client feedback still needs manual coverage.

Chunk lifecycle tests wait for a real remote unload, retain the link without
loading the twin for 45 ticks, then remove the source and reload/reconcile the
surviving twin. A transfer test checks entity arrival and actual destination
unload 350 ticks later, after the temporary portal ticket expires.

## Manual checks

Run `./gradlew build`, then `./gradlew runClient`. Use a disposable creative
world with commands enabled. These are manual checks; a successful build alone
does not establish gameplay correctness.

## Arrival clearance

1. Place, pair, and power two teleporters. With clear space above the destination,
   walk onto the source. Expect one transfer and no immediate return.
2. Leave the destination portal intact and put a bottom slab in the block above
   it. Expect the transfer to be refused and the obstruction message to appear.
3. Repeat with a closed trapdoor, a fence, and a full block at head height.
   Each intersecting collision shape must prevent transfer.
4. Put fire or lava above the destination. Expect refusal. Repeat with fire in
   the upper part of the arriving entity's volume.
5. Use a wide or tall mob and place an obstacle that intersects its arrival
   volume but lies outside a player's volume. Expect refusal for the mob.
6. Remove the obstacles, leave the source until its cooldown expires, and retry.
   Expect successful transport; obstruction must not destroy the pair.
7. Repeat the clearance checks with the destination in another dimension.

## Link recovery

1. Pair two portals, then run `/data remove block <x> <y> <z> twin_target`
   against one portal. Expect that portal to become unlinked on its next server
   tick without requiring a click or a Redstone update.
2. Keep both portals loaded and break one. Expect its twin to become unlinked.
3. Unload the surviving twin before breaking the source. Reload both locations
   and wait at least 40 ticks. Expect the surviving twin to become unlinked.
   If the missing source's chunk remains unloaded, verification is deferred
   until it can be inspected or a transport attempt resolves it.
4. Save and reopen a world containing a valid pair. Expect the pair to persist.
5. Pair B with C, leave A unlinked, and use `/data merge block <Ax> <Ay> <Az>
   {twin_target:{dimension:"minecraft:overworld",pos:[I;<Bx>,<By>,<Bz>]}}`
   to give A a stale reference to B. Break A immediately. B and C must remain
   paired: removal must only clear a twin that actually points back to A.
6. Disconnect a valid pair using an empty tuner while sneaking. Both loaded
   endpoints must become unlinked. Repeat across dimensions.

## Redstone and cooldown

1. Remove power from the source. Expect teleportation to stop immediately.
2. Restore power and transfer. Remain on the destination for more than 60 ticks.
   Expect no return while standing on the pad.
3. Step away, wait at least 40 ticks, and return. Expect reverse transport.
