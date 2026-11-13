package com.project.pointportals;

import com.project.pointportals.block.TeleporterBlock;
import com.project.pointportals.block.entity.TeleporterBlockEntity;
import com.project.pointportals.component.PortalTarget;
import com.project.pointportals.registry.ModBlocks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class ChunkLifecycleGameTests {
	@GameTest(maxTicks = 600)
	public void unloadedTwinIsRetainedAndReconciledAfterSourceRemoval(GameTestHelper test) {
		var source = test.getLevel();
		var remote = source.getServer().getLevel(Level.NETHER);
		test.assertTrue(remote != null, "Nether must exist");
		var localPos = test.absolutePos(new BlockPos(2, 1, 2));
		var remotePos = new BlockPos(localPos.getX() + 10000, 240, localPos.getZ());
		source.setBlock(localPos, ModBlocks.TELEPORTER.defaultBlockState(), Block.UPDATE_ALL);
		remote.getChunk(remotePos);
		remote.setBlock(remotePos, ModBlocks.TELEPORTER.defaultBlockState(), Block.UPDATE_ALL);
		var a = (TeleporterBlockEntity) source.getBlockEntity(localPos);
		var b = (TeleporterBlockEntity) remote.getBlockEntity(remotePos);
		a.setTwinTarget(PortalTarget.of(remote, remotePos));
		b.setTwinTarget(PortalTarget.of(source, localPos));
		source.setBlock(localPos, a.getBlockState().setValue(TeleporterBlock.LINKED, true), Block.UPDATE_ALL);
		remote.setBlock(remotePos, b.getBlockState().setValue(TeleporterBlock.LINKED, true), Block.UPDATE_ALL);
		long[] unloadedAt = {-1};
		boolean[] done = {false};
		Runnable cleanup = () -> {
			done[0] = true;
			source.setBlock(localPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			remote.getChunk(remotePos);
			remote.setBlock(remotePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		};
		test.onEachTick(() -> {
			if (done[0]) return;
			try {
				if (unloadedAt[0] < 0) {
					if (remote.isLoaded(remotePos)) return;
					unloadedAt[0] = test.getTick();
				}
				test.assertFalse(remote.isLoaded(remotePos), "Periodic verification must not load remote chunk");
				test.assertTrue(a.verifyAndSyncLink(false) && a.isLinked(), "Unloaded twin must retain its link");
				test.assertFalse(remote.isLoaded(remotePos), "Verification must leave chunk unloaded");
				if (test.getTick() - unloadedAt[0] < 45) return;
				source.setBlock(localPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
				test.assertFalse(remote.isLoaded(remotePos), "Breaking source must not load twin");
				remote.getChunk(remotePos);
				var restored = (TeleporterBlockEntity) remote.getBlockEntity(remotePos);
				test.assertTrue(restored != null && restored.isLinked(), "Chunk reload must restore persisted twin reference");
				test.assertFalse(restored.verifyAndSyncLink(false), "Reloaded twin must detect missing source");
				test.assertTrue(!restored.isLinked() && !restored.getBlockState().getValue(TeleporterBlock.LINKED), "Broken pair must reset destination and state");
				cleanup.run();
				test.succeed();
			} catch (RuntimeException | Error failure) {
				cleanup.run();
				throw failure;
			}
		});
		test.runAfterDelay(550, () -> {
			if (!done[0]) {
				cleanup.run();
				test.assertTrue(false, "Remote chunk never completed real unload/reload lifecycle");
			}
		});
	}
}
