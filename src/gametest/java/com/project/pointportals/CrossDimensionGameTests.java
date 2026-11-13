package com.project.pointportals;

import com.project.pointportals.block.TeleporterBlock;
import com.project.pointportals.block.entity.TeleporterBlockEntity;
import com.project.pointportals.component.PortalTarget;
import com.project.pointportals.registry.ModBlocks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

public class CrossDimensionGameTests {
	@GameTest
	public void overworldToNether(GameTestHelper test) {
		transfer(test, false);
	}

	@GameTest
	public void netherToOverworld(GameTestHelper test) {
		transfer(test, true);
	}

	private static void transfer(GameTestHelper test, boolean reverse) {
		try (var pair = new DimensionPair(test)) {
			ServerLevel source = reverse ? pair.nether : test.getLevel();
			ServerLevel destination = reverse ? test.getLevel() : pair.nether;
			BlockPos sourcePos = reverse ? pair.remote : pair.local;
			BlockPos destinationPos = reverse ? pair.local : pair.remote;
			var a = (TeleporterBlockEntity) source.getBlockEntity(sourcePos);
			var b = (TeleporterBlockEntity) destination.getBlockEntity(destinationPos);
			var pig = EntityTypes.PIG.spawn(source, sourcePos.above(), EntitySpawnReason.COMMAND);
			test.assertTrue(pig != null, "Test pig must spawn");
			pig.setNoAi(true);
			pig.setYRot(37.0F);
			pig.setXRot(12.0F);
			var uuid = pig.getUUID();
			try {
				test.assertTrue(a.teleportEntity(source, sourcePos, a.getBlockState(), pig), "Cross-dimension transfer must succeed");
				// Minecraft may replace a non-player entity when changing dimensions.
				Entity arrived = destination.getEntityInAnyDimension(uuid);
				test.assertTrue(arrived != null && arrived.level() == destination, "UUID must resolve in destination dimension");
				test.assertTrue(Math.abs(arrived.getX() - destinationPos.getX() - 0.5) < 0.01 &&
					Math.abs(arrived.getY() - destinationPos.getY() - 1.0) < 0.01 &&
					Math.abs(arrived.getZ() - destinationPos.getZ() - 0.5) < 0.01, "Arrival must use exact portal coordinates without Nether scaling");
				test.assertTrue(Math.abs(arrived.getYRot() - 37.0F) < 0.01 &&
					Math.abs(arrived.getXRot() - 12.0F) < 0.01, "Transfer must preserve rotation");
				test.assertTrue(source.getEntities((Entity) null, new AABB(sourcePos).inflate(4), e -> e.getUUID().equals(uuid)).isEmpty(),
					"Source must not retain a duplicate entity");
				test.assertTrue(arrived.getPortalCooldown() >= 40, "Arriving entity must retain at least 40 ticks of cooldown");
				test.assertFalse(b.teleportEntity(destination, destinationPos, b.getBlockState(), arrived), "Immediate return must be blocked");
				test.assertTrue(a.verifyAndSyncLink(false) && b.verifyAndSyncLink(false), "Transfer must preserve reciprocal link");
				test.succeed();
			} finally {
				Entity remaining = source.getEntityInAnyDimension(uuid);
				if (remaining != null) remaining.discard();
			}
		}
	}

	@GameTest
	public void obstructedNetherArrivalKeepsEntityAtSource(GameTestHelper test) {
		try (var pair = new DimensionPair(test)) {
			pair.nether.setBlock(pair.remote.above(), Blocks.STONE_SLAB.defaultBlockState(), Block.UPDATE_ALL);
			var a = (TeleporterBlockEntity) test.getLevel().getBlockEntity(pair.local);
			var pig = test.spawn(EntityTypes.PIG, new BlockPos(2, 2, 2));
			pig.setNoAi(true);
			try {
				test.assertFalse(a.teleportEntity(test.getLevel(), pair.local, a.getBlockState(), pig), "Obstruction in Nether must refuse transfer");
				test.assertTrue(!pig.isRemoved() && pig.level() == test.getLevel(), "Refused entity must remain alive in source dimension");
				test.assertTrue(a.verifyAndSyncLink(false), "Obstruction must preserve the pair");
				test.succeed();
			} finally {
				pig.discard();
			}
		}
	}

	/** Remote fixtures are isolated above the Nether roof in the generated test world. */
	private static final class DimensionPair implements AutoCloseable {
		final ServerLevel nether;
		final BlockPos local;
		final BlockPos remote;

		DimensionPair(GameTestHelper test) {
			nether = test.getLevel().getServer().getLevel(Level.NETHER);
			test.assertTrue(nether != null, "Nether dimension must exist");
			local = test.absolutePos(new BlockPos(2, 1, 2));
			remote = new BlockPos(local.getX(), 240, local.getZ());
			nether.getChunk(remote);
			for (BlockPos pos : BlockPos.betweenClosed(remote.offset(-1, -1, -1), remote.offset(1, 3, 1))) {
				nether.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
			test.getLevel().setBlock(local, ModBlocks.TELEPORTER.defaultBlockState(), Block.UPDATE_ALL);
			nether.setBlock(remote, ModBlocks.TELEPORTER.defaultBlockState(), Block.UPDATE_ALL);
			var a = (TeleporterBlockEntity) test.getLevel().getBlockEntity(local);
			var b = (TeleporterBlockEntity) nether.getBlockEntity(remote);
			a.setTwinTarget(PortalTarget.of(nether, remote));
			b.setTwinTarget(PortalTarget.of(test.getLevel(), local));
			test.getLevel().setBlock(local, a.getBlockState().setValue(TeleporterBlock.LINKED, true), Block.UPDATE_CLIENTS);
			nether.setBlock(remote, b.getBlockState().setValue(TeleporterBlock.LINKED, true), Block.UPDATE_CLIENTS);
			test.getLevel().setBlock(local.below(), Blocks.REDSTONE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
			nether.setBlock(remote.below(), Blocks.REDSTONE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
		}

		@Override
		public void close() {
			for (BlockPos pos : BlockPos.betweenClosed(remote.offset(-1, -1, -1), remote.offset(1, 3, 1))) {
				nether.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			}
		}
	}
}
