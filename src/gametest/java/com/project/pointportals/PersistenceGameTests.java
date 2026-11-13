package com.project.pointportals;

import com.project.pointportals.block.TeleporterBlock;
import com.project.pointportals.block.entity.TeleporterBlockEntity;
import com.project.pointportals.component.PortalTarget;
import com.project.pointportals.registry.ModBlocks;
import com.project.pointportals.registry.ModDataComponents;
import com.project.pointportals.registry.ModItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

public class PersistenceGameTests {
	@GameTest
	public void reciprocalPairSurvivesSerialization(GameTestHelper test) {
		var a = portal(test, new BlockPos(1, 1, 1));
		var b = portal(test, new BlockPos(4, 1, 1));
		a.setTwinTarget(PortalTarget.of(test.getLevel(), b.getBlockPos()));
		b.setTwinTarget(PortalTarget.of(test.getLevel(), a.getBlockPos()));
		for (var original : new TeleporterBlockEntity[] {a, b}) {
			var saved = original.saveWithFullMetadata(test.getLevel().registryAccess());
			var restored = (TeleporterBlockEntity) BlockEntity.loadStatic(original.getBlockPos(), original.getBlockState(), saved, test.getLevel().registryAccess());
			test.assertTrue(restored != null && original.getTwinTarget().equals(restored.getTwinTarget()), "Disk metadata must restore exact twin coordinates and dimension");
			original.setTwinTarget(null);
			original.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, test.getLevel().registryAccess(), saved));
		}
		test.assertTrue(a.verifyAndSyncLink(false) && b.verifyAndSyncLink(false), "Restored endpoints must remain reciprocal");
		test.succeed();
	}

	@GameTest
	public void absentAndMalformedTargetsResetLinkedState(GameTestHelper test) {
		var a = portal(test, new BlockPos(1, 1, 1));
		for (boolean malformed : new boolean[] {false, true}) {
			a.setTwinTarget(PortalTarget.of(test.getLevel(), a.getBlockPos().east()));
			test.setBlock(new BlockPos(1, 1, 1), a.getBlockState().setValue(TeleporterBlock.LINKED, true));
			var tag = new CompoundTag();
			if (malformed) tag.putString("twin_target", "invalid target");
			a.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, test.getLevel().registryAccess(), tag));
			TeleporterBlockEntity.serverTick(test.getLevel(), a.getBlockPos(), a.getBlockState(), a);
			test.assertTrue(a.getTwinTarget() == null && !a.getBlockState().getValue(TeleporterBlock.LINKED), "Missing or invalid disk data must clear both destination and state");
		}
		var restored = (TeleporterBlockEntity) BlockEntity.loadStatic(a.getBlockPos(), a.getBlockState(), a.saveWithFullMetadata(test.getLevel().registryAccess()), test.getLevel().registryAccess());
		test.assertTrue(restored != null && !restored.isLinked(), "Unlinked portal must remain unlinked after reload");
		test.succeed();
	}

	@GameTest
	public void nonexistentDimensionIsReconciledAfterReload(GameTestHelper test) {
		var a = portal(test, new BlockPos(1, 1, 1));
		var target = new PortalTarget(ResourceKey.create(Registries.DIMENSION, Identifier.parse("pointportals:missing_test_dimension")), new BlockPos(12, 80, 34));
		a.setTwinTarget(target);
		var saved = a.saveCustomOnly(test.getLevel().registryAccess());
		a.setTwinTarget(null);
		a.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, test.getLevel().registryAccess(), saved));
		test.assertTrue(target.equals(a.getTwinTarget()), "Valid dimension identifier must deserialize even when world is missing");
		test.assertFalse(a.verifyAndSyncLink(false), "Missing world must invalidate link");
		test.assertTrue(a.getTwinTarget() == null, "Missing world reference must be cleared");
		test.succeed();
	}

	@GameTest
	public void tunerStackPersistsSelection(GameTestHelper test) {
		var stack = new ItemStack(ModItems.TUNER);
		var target = PortalTarget.of(test.getLevel(), test.absolutePos(new BlockPos(2, 1, 2)));
		stack.set(ModDataComponents.PORTAL_TARGET, target);
		var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, test.getLevel().registryAccess());
		output.store("stack", ItemStack.CODEC, stack);
		var input = TagValueInput.create(ProblemReporter.DISCARDING, test.getLevel().registryAccess(), output.buildResult());
		var restored = input.read("stack", ItemStack.CODEC).orElseThrow();
		test.assertTrue(restored.is(ModItems.TUNER) && target.equals(restored.get(ModDataComponents.PORTAL_TARGET)), "Full ItemStack persistence must preserve tuner selection");
		test.succeed();
	}

	private static TeleporterBlockEntity portal(GameTestHelper test, BlockPos pos) {
		test.setBlock(pos, ModBlocks.TELEPORTER);
		return (TeleporterBlockEntity) test.getLevel().getBlockEntity(test.absolutePos(pos));
	}
}
