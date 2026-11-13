package com.project.pointportals;

import com.project.pointportals.block.TeleporterBlock;
import com.project.pointportals.block.entity.TeleporterBlockEntity;
import com.project.pointportals.component.PortalTarget;
import com.project.pointportals.registry.ModBlocks;
import com.project.pointportals.registry.ModDataComponents;
import com.project.pointportals.registry.ModItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class TunerGameTests {
	private static final BlockPos A = new BlockPos(1, 1, 1);
	private static final BlockPos B = new BlockPos(4, 1, 1);
	private static final BlockPos C = new BlockPos(4, 1, 4);

	@GameTest
	public void firstClickStoresTargetWithoutLinking(GameTestHelper test) {
		var a = portal(test, A);
		var tuner = new Tuner(test);
		test.assertTrue(tuner.click(A) == InteractionResult.SUCCESS, "First click must succeed");
		test.assertTrue(PortalTarget.of(test.getLevel(), a.getBlockPos()).equals(tuner.target()), "Component must store position and dimension");
		assertUnlinked(test, a);
		test.succeed();
	}

	@GameTest
	public void secondClickLinksAndClearsSelection(GameTestHelper test) {
		var a = portal(test, A);
		var b = portal(test, B);
		test.setBlock(A.below(), Blocks.REDSTONE_BLOCK);
		var tuner = new Tuner(test);
		tuner.click(A);
		test.assertTrue(tuner.click(B) == InteractionResult.SUCCESS, "Second click must succeed");
		assertPair(test, a, b);
		test.assertTrue(tuner.target() == null, "Successful pairing must clear selection");
		test.assertTrue(a.getBlockState().getValue(TeleporterBlock.POWERED), "Pairing must preserve source power");
		test.assertFalse(b.getBlockState().getValue(TeleporterBlock.POWERED), "Unpowered twin must remain in standby");
		test.succeed();
	}

	@GameTest
	public void selfLinkIsRejectedWithoutLosingSelection(GameTestHelper test) {
		var a = portal(test, A);
		var tuner = new Tuner(test);
		tuner.click(A);
		var selection = tuner.target();
		test.assertTrue(tuner.click(A) == InteractionResult.FAIL, "Self-link must fail");
		test.assertTrue(selection.equals(tuner.target()), "Self-link rejection must preserve selection");
		assertUnlinked(test, a);
		test.succeed();
	}

	@GameTest
	public void occupiedDestinationPreservesBothPairAndSelection(GameTestHelper test) {
		var a = portal(test, A);
		var b = portal(test, B);
		var c = portal(test, C);
		var owner = new Tuner(test);
		owner.click(B);
		owner.click(C);
		var contender = new Tuner(test);
		contender.click(A);
		var selection = contender.target();
		test.assertTrue(contender.click(B) == InteractionResult.FAIL, "Occupied destination must reject pairing");
		test.assertTrue(selection.equals(contender.target()), "Contender must retain its selection");
		assertUnlinked(test, a);
		assertPair(test, b, c);
		test.assertTrue(owner.click(B) == InteractionResult.FAIL, "Empty tuner must not select an occupied portal");
		test.assertTrue(owner.target() == null, "Occupied portal must not be stored");
		test.succeed();
	}

	@GameTest
	public void sourceClaimedByAnotherTunerCannotBeOverwritten(GameTestHelper test) {
		var a = portal(test, A);
		var b = portal(test, B);
		var c = portal(test, C);
		var waiting = new Tuner(test);
		waiting.click(A);
		var owner = new Tuner(test);
		owner.click(A);
		owner.click(C);
		test.assertTrue(waiting.click(B) == InteractionResult.FAIL, "Previously selected source is now occupied");
		test.assertTrue(waiting.target() == null, "Obsolete selection must be cleared");
		assertPair(test, a, c);
		assertUnlinked(test, b);
		test.succeed();
	}

	@GameTest
	public void missingSourceClearsSelectionWithoutLinkingDestination(GameTestHelper test) {
		portal(test, A);
		var b = portal(test, B);
		var tuner = new Tuner(test);
		tuner.click(A);
		test.setBlock(A, Blocks.AIR);
		test.assertTrue(tuner.click(B) == InteractionResult.FAIL, "Missing source must reject pairing");
		test.assertTrue(tuner.target() == null, "Missing source must clear selection");
		assertUnlinked(test, b);
		test.succeed();
	}

	@GameTest
	public void sneakingWithSelectionClearsMemoryBeforeUnlinking(GameTestHelper test) {
		var a = portal(test, A);
		var b = portal(test, B);
		portal(test, C);
		var owner = new Tuner(test);
		owner.click(A);
		owner.click(B);
		var tuner = new Tuner(test);
		tuner.click(C);
		tuner.player.setShiftKeyDown(true);
		test.assertTrue(tuner.click(A) == InteractionResult.SUCCESS, "Sneak click must clear memory");
		test.assertTrue(tuner.target() == null, "Selection must be cleared");
		assertPair(test, a, b);
		test.succeed();
	}

	@GameTest
	public void sneakingWithEmptyTunerUnlinksBothEnds(GameTestHelper test) {
		var a = portal(test, A);
		var b = portal(test, B);
		var tuner = new Tuner(test);
		tuner.click(A);
		tuner.click(B);
		tuner.player.setShiftKeyDown(true);
		test.assertTrue(tuner.click(A) == InteractionResult.SUCCESS, "Sneak click must unlink pair");
		assertUnlinked(test, a);
		assertUnlinked(test, b);
		test.assertTrue(tuner.target() == null, "Unlink must leave tuner empty");
		test.succeed();
	}

	@GameTest
	public void otherBlocksDoNotConsumeSelection(GameTestHelper test) {
		portal(test, A);
		test.setBlock(B, Blocks.STONE);
		var tuner = new Tuner(test);
		tuner.click(A);
		var selection = tuner.target();
		test.assertTrue(tuner.click(B) == InteractionResult.PASS, "Non-portals must pass interaction");
		test.assertTrue(selection.equals(tuner.target()), "Other blocks must preserve selection");
		test.succeed();
	}

	private static TeleporterBlockEntity portal(GameTestHelper test, BlockPos pos) {
		test.setBlock(pos, ModBlocks.TELEPORTER);
		return (TeleporterBlockEntity) test.getLevel().getBlockEntity(test.absolutePos(pos));
	}

	private static void assertUnlinked(GameTestHelper test, TeleporterBlockEntity portal) {
		test.assertTrue(portal.getTwinTarget() == null, "Unlinked portal must have no destination");
		test.assertFalse(portal.getBlockState().getValue(TeleporterBlock.LINKED), "Unlinked blockstate must match destination");
	}

	private static void assertPair(GameTestHelper test, TeleporterBlockEntity a, TeleporterBlockEntity b) {
		test.assertTrue(PortalTarget.of(b.getLevel(), b.getBlockPos()).equals(a.getTwinTarget()), "A must point to B");
		test.assertTrue(PortalTarget.of(a.getLevel(), a.getBlockPos()).equals(b.getTwinTarget()), "B must point to A");
		test.assertTrue(a.getBlockState().getValue(TeleporterBlock.LINKED) &&
			b.getBlockState().getValue(TeleporterBlock.LINKED), "Both blockstates must be linked");
	}

	private static final class Tuner {
		final GameTestHelper test;
		final Player player;
		final ItemStack stack = new ItemStack(ModItems.TUNER);

		Tuner(GameTestHelper test) {
			this.test = test;
			player = test.makeMockPlayer(GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		}

		PortalTarget target() {
			return stack.get(ModDataComponents.PORTAL_TARGET);
		}

		InteractionResult click(BlockPos relative) {
			BlockPos pos = test.absolutePos(relative);
			var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
			return ModItems.TUNER.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
		}
	}
}
