package com.project.pointportals;

import com.project.pointportals.block.TeleporterBlock;
import com.project.pointportals.block.entity.TeleporterBlockEntity;
import com.project.pointportals.component.PortalTarget;
import com.project.pointportals.registry.ModBlocks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class PortalGameTests {
	private static final BlockPos A = new BlockPos(1, 1, 1);
	private static final BlockPos B = new BlockPos(5, 1, 1);
	private static final BlockPos C = new BlockPos(5, 1, 5);

	@GameTest
	public void reciprocalPairIsValid(GameTestHelper test) {
		var a = portal(test, A);
		var b = portal(test, B);
		pair(test, a, b);
		test.assertTrue(a.verifyAndSyncLink(false), "A must verify B");
		test.assertTrue(b.verifyAndSyncLink(false), "B must verify A");
		test.succeed();
	}

	@GameTest
	public void staleRemovalPreservesOtherPair(GameTestHelper test) {
		var a = portal(test, A);
		var b = portal(test, B);
		var c = portal(test, C);
		pair(test, b, c);
		a.setTwinTarget(PortalTarget.of(test.getLevel(), b.getBlockPos()));
		test.getLevel().destroyBlock(a.getBlockPos(), false);
		test.assertTrue(b.verifyAndSyncLink(false), "Removing stale A must preserve B-C");
		test.assertTrue(c.verifyAndSyncLink(false), "C must still point back to B");
		test.succeed();
	}

	@GameTest
	public void breakingTwinClearsLink(GameTestHelper test) {
		var a = portal(test, A);
		var b = portal(test, B);
		pair(test, a, b);
		test.getLevel().destroyBlock(a.getBlockPos(), false);
		test.assertFalse(b.isLinked(), "Breaking A must clear B's destination");
		test.assertFalse(b.getBlockState().getValue(TeleporterBlock.LINKED), "B must become inactive");
		test.succeed();
	}

	@GameTest
	public void missingDestinationHealsOnTick(GameTestHelper test) {
		var a = portal(test, A);
		test.getLevel().setBlock(a.getBlockPos(), a.getBlockState().setValue(TeleporterBlock.LINKED, true), Block.UPDATE_CLIENTS);
		test.runAfterDelay(2, () -> {
			test.assertFalse(a.getBlockState().getValue(TeleporterBlock.LINKED), "Tick must heal missing destination");
			test.succeed();
		});
	}

	@GameTest
	public void slabBlocksArrival(GameTestHelper test) {
		var a = portal(test, A);
		var b = portal(test, B);
		pair(test, a, b);
		test.setBlock(B.above(), Blocks.STONE_SLAB);
		test.setBlock(A.below(), Blocks.REDSTONE_BLOCK);
		test.setBlock(B.below(), Blocks.REDSTONE_BLOCK);
		Pig pig = test.spawn(EntityTypes.PIG, A.above());
		pig.setNoAi(true);
		test.assertFalse(a.teleportEntity(test.getLevel(), a.getBlockPos(),
			a.getBlockState(), pig), "Partial collision must reject arrival");
		test.assertTrue(pig.getX() < b.getBlockPos().getX(), "Rejected entity must remain at source");
		test.assertTrue(a.isLinked() && b.isLinked(), "Obstruction must preserve the pair");
		test.succeed();
	}

	@GameTest
	public void successfulTransferPreventsImmediateReturn(GameTestHelper test) {
		var a = portal(test, A);
		var b = portal(test, B);
		pair(test, a, b);
		test.setBlock(A.below(), Blocks.REDSTONE_BLOCK);
		test.setBlock(B.below(), Blocks.REDSTONE_BLOCK);
		Pig pig = test.spawn(EntityTypes.PIG, A.above());
		pig.setNoAi(true);
		test.assertTrue(a.teleportEntity(test.getLevel(), a.getBlockPos(),
			a.getBlockState(), pig), "Clear destination must allow transfer");
		test.assertTrue(Math.abs(pig.getX() - (b.getBlockPos().getX() + 0.5)) < 0.01, "Entity must arrive centered");
		test.assertTrue(pig.isOnPortalCooldown(), "Transfer must apply cooldown");
		test.assertFalse(b.teleportEntity(test.getLevel(), b.getBlockPos(),
			b.getBlockState(), pig), "Cooldown must prevent immediate return");
		test.succeed();
	}

	@GameTest
	public void powerCutStopsTransfer(GameTestHelper test) {
		var a = portal(test, A);
		var b = portal(test, B);
		pair(test, a, b);
		test.setBlock(A.below(), Blocks.REDSTONE_BLOCK);
		test.assertTrue(a.getBlockState().getValue(TeleporterBlock.POWERED), "Redstone must activate source");
		test.setBlock(A.below(), Blocks.AIR);
		test.assertFalse(a.getBlockState().getValue(TeleporterBlock.POWERED), "Power cut must deactivate immediately");
		Pig pig = test.spawn(EntityTypes.PIG, A.above());
		pig.setNoAi(true);
		test.assertFalse(a.teleportEntity(test.getLevel(), a.getBlockPos(), a.getBlockState(), pig),
			"Unpowered portal must refuse transfer");
		test.assertTrue(a.isLinked() && b.isLinked(), "Power cut must preserve the pair");
		test.succeed();
	}

	private static TeleporterBlockEntity portal(GameTestHelper test, BlockPos relative) {
		test.setBlock(relative, ModBlocks.TELEPORTER);
		return (TeleporterBlockEntity) test.getLevel().getBlockEntity(test.absolutePos(relative));
	}

	private static void pair(GameTestHelper test, TeleporterBlockEntity a, TeleporterBlockEntity b) {
		a.setTwinTarget(PortalTarget.of(test.getLevel(), b.getBlockPos()));
		b.setTwinTarget(PortalTarget.of(test.getLevel(), a.getBlockPos()));
		for (var portal : new TeleporterBlockEntity[] {a, b}) {
			test.getLevel().setBlock(portal.getBlockPos(), portal.getBlockState()
				.setValue(TeleporterBlock.LINKED, true), Block.UPDATE_CLIENTS);
		}
	}
}
