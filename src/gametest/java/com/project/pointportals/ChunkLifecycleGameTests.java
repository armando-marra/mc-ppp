package com.project.pointportals;

import com.project.pointportals.block.TeleporterBlock;
import com.project.pointportals.block.entity.TeleporterBlockEntity;
import com.project.pointportals.cartography.PortalMapIndex;
import com.project.pointportals.cartography.PortalMapMarker;
import com.project.pointportals.component.PortalTarget;
import com.project.pointportals.registry.ModBlocks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.MapItem;
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
		a.setLabel("Casa");
		b.setLabel("Miniera");
		var localMap = MapItem.create(source, localPos.getX(), localPos.getZ(), (byte) 0, false, false);
		var remoteMap = MapItem.create(remote, remotePos.getX(), remotePos.getZ(), (byte) 0, false, false);
		PortalMapMarker.storage(MapItem.getSavedData(localMap, source)).pointportals$putMarker(PortalMapIndex.register(source, a));
		test.assertTrue(PortalMapIndex.pairs(source, MapItem.getSavedData(localMap, source)).isEmpty(),
			"Map details must omit a twin which has not been mapped");
		PortalMapMarker.storage(MapItem.getSavedData(remoteMap, remote)).pointportals$putMarker(PortalMapIndex.register(remote, b));
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
				var details = PortalMapIndex.pairs(source, MapItem.getSavedData(localMap, source));
				test.assertTrue(details.size() == 1 && details.getFirst().getContents() instanceof TranslatableContents text
					&& text.getArgs()[0] instanceof Component left && left.getString().equals("Casa")
					&& text.getArgs()[1] instanceof Component right && right.getString().equals("Miniera"),
					"Mapped ends in different dimensions must form a named pair");
				test.assertTrue(PortalMapMarker.storage(MapItem.getSavedData(remoteMap, source)).pointportals$hasMarker(remotePos),
					"Unloading the remote chunk must keep its marker");
				test.assertFalse(remote.isLoaded(remotePos), "Reading both maps must leave the remote chunk unloaded");
				if (test.getTick() - unloadedAt[0] < 45) return;
				a.clearAndReset(true);
				test.assertTrue(PortalMapIndex.pairs(source, MapItem.getSavedData(localMap, source)).isEmpty(),
					"Unlinking with the twin unloaded must immediately hide the pair");
				test.assertTrue(PortalMapMarker.storage(MapItem.getSavedData(localMap, source)).pointportals$hasMarker(localPos),
					"Unlinking must preserve the local marker");
				a.setTwinTarget(PortalTarget.of(remote, remotePos));
				source.setBlock(localPos, a.getBlockState().setValue(TeleporterBlock.LINKED, true), Block.UPDATE_ALL);
				source.setBlock(localPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
				test.assertFalse(PortalMapMarker.storage(MapItem.getSavedData(localMap, source)).pointportals$hasMarker(localPos),
					"Breaking a portal must remove its live marker");
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
