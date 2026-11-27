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
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class TunerGameTests {
	@GameTest
	public void playerRightClickPairsPortalsInSurvival(GameTestHelper test) {
		playerRightClickPairsPortals(test, GameType.SURVIVAL, InteractionHand.MAIN_HAND);
	}

	@GameTest
	public void playerRightClickPairsPortalsInCreative(GameTestHelper test) {
		playerRightClickPairsPortals(test, GameType.CREATIVE, InteractionHand.MAIN_HAND);
	}

	@GameTest
	public void playerOffhandRightClickPairsPortals(GameTestHelper test) {
		playerRightClickPairsPortals(test, GameType.SURVIVAL, InteractionHand.OFF_HAND);
	}

	@SuppressWarnings("removal")
	private static void playerRightClickPairsPortals(GameTestHelper test, GameType mode, InteractionHand hand) {
		var a = portal(test, A);
		var b = portal(test, B);
		var player = test.makeMockServerPlayerInLevel();
		player.setGameMode(mode);
		var stack = new ItemStack(ModItems.TUNER);
		player.setItemInHand(hand, stack);
		try {
			test.assertTrue(playerClick(player, hand, a.getBlockPos()).consumesAction(), "First player click must succeed");
			test.assertTrue(PortalTarget.of(test.getLevel(), a.getBlockPos()).equals(stack.get(ModDataComponents.PORTAL_TARGET)),
				"Normal player interaction must reach the tuner and store the first portal");
			test.assertTrue(playerClick(player, hand, b.getBlockPos()).consumesAction(), "Second player click must succeed");
			assertPair(test, a, b);
			test.assertTrue(!stack.has(ModDataComponents.PORTAL_TARGET), "Pairing must clear the tuner selection");
			player.setShiftKeyDown(true);
			test.assertTrue(playerClick(player, hand, a.getBlockPos()).consumesAction(), "Sneak click must unlink");
			assertUnlinked(test, a);
			assertUnlinked(test, b);
			player.setShiftKeyDown(false);
			player.setItemInHand(hand, ItemStack.EMPTY);
			test.assertTrue(playerClick(player, InteractionHand.MAIN_HAND, a.getBlockPos()).consumesAction(),
				"Empty-hand portal inspection must still work");
			test.succeed();
		} finally {
			test.getLevel().getServer().getPlayerList().remove(player);
			player.discard();
		}
	}

	@GameTest
	@SuppressWarnings("removal")
	public void labelBeforePairing(GameTestHelper test) {
		var portal = portal(test, A);
		var twin = portal(test, B);
		var player = test.makeMockServerPlayerInLevel();
		var tag = new ItemStack(Items.NAME_TAG);
		var name = DataComponents.CUSTOM_NAME;
		try {
			player.setGameMode(GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, tag);
			tag.set(name, Component.literal("Miniere del Nord"));
			playerClick(player, InteractionHand.MAIN_HAND, portal.getBlockPos());
			var saved = portal.saveWithFullMetadata(test.getLevel().registryAccess());
			var restored = (TeleporterBlockEntity) BlockEntity.loadStatic(
				portal.getBlockPos(), portal.getBlockState(), saved, test.getLevel().registryAccess());
			test.assertTrue(restored != null && restored.getLabel().equals("Miniere del Nord"), "Unlinked pad must save its label");
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.TUNER));
			playerClick(player, InteractionHand.MAIN_HAND, portal.getBlockPos());
			playerClick(player, InteractionHand.MAIN_HAND, twin.getBlockPos());
			test.assertTrue(twin.getLabel().isEmpty(), "Each endpoint owns its own label");
			test.getLevel().setBlockAndUpdate(portal.getBlockPos().below(), Blocks.REDSTONE_BLOCK.defaultBlockState());
			test.assertTrue(portal.getBlockState().getValue(TeleporterBlock.POWERED) && portal.getLabel().equals("Miniere del Nord"),
				"Powering the linked portal must preserve the original label");
			player.setItemInHand(InteractionHand.MAIN_HAND, tag);
			tag.set(name, Component.literal("A".repeat(25)));
			playerClick(player, InteractionHand.MAIN_HAND, portal.getBlockPos());
			test.assertTrue(portal.getLabel().equals("Miniere del Nord"), "Oversized label must leave previous text intact");
			tag.set(name, Component.literal("Città"));
			playerClick(player, InteractionHand.MAIN_HAND, portal.getBlockPos());
			test.assertTrue(portal.getLabel().equals("Città"), "Named tag must replace the active label");
			tag.remove(name);
			playerClick(player, InteractionHand.MAIN_HAND, portal.getBlockPos());
			test.assertTrue(portal.getLabel().isEmpty(), "Plain name tag must clear the label");
			test.succeed();
		} finally {
			test.getLevel().getServer().getPlayerList().remove(player);
			player.discard();
		}
	}

	private static InteractionResult playerClick(ServerPlayer player, InteractionHand hand, BlockPos pos) {
		return player.gameMode.useItemOn(player, player.level(), player.getItemInHand(hand), hand,
			new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
	}

	@GameTest
	public void crossDimensionPairAndUnlink(GameTestHelper test) {
		crossDimensionTuner(test, 0);
	}

	@GameTest
	public void crossDimensionOccupiedDestinationIsPreserved(GameTestHelper test) {
		crossDimensionTuner(test, 1);
	}

	@GameTest
	public void crossDimensionMissingSourceClearsSelection(GameTestHelper test) {
		crossDimensionTuner(test, 2);
	}

	// Supplies the embedded connection used by the real dimension transfer.
	@SuppressWarnings("removal")
	private static void crossDimensionTuner(GameTestHelper test, int scenario) {
		var remote = test.getLevel().getServer().getLevel(Level.NETHER);
		test.assertTrue(remote != null, "Nether must exist");
		var a = portal(test, A);
		var c = portal(test, C);
		var pos = new BlockPos(a.getBlockPos().getX(), 240, a.getBlockPos().getZ());
		remote.getChunk(pos);
		remote.setBlock(pos, ModBlocks.TELEPORTER.defaultBlockState(), Block.UPDATE_ALL);
		var b = (TeleporterBlockEntity) remote.getBlockEntity(pos);
		var player = test.makeMockServerPlayerInLevel();
		var stack = new ItemStack(ModItems.TUNER);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		try {
			test.setBlock(A.below(), Blocks.REDSTONE_BLOCK);
			if (scenario == 1) {
				c.setTwinTarget(PortalTarget.of(remote, pos));
				b.setTwinTarget(PortalTarget.of(test.getLevel(), c.getBlockPos()));
				test.setBlock(C, c.getBlockState().setValue(TeleporterBlock.LINKED, true));
				remote.setBlock(pos, b.getBlockState().setValue(TeleporterBlock.LINKED, true), Block.UPDATE_ALL);
			}
			test.assertTrue(click(player, a.getBlockPos()) == InteractionResult.SUCCESS, "Source selection must succeed");
			var selection = stack.get(ModDataComponents.PORTAL_TARGET);
			if (scenario == 2) test.setBlock(A, Blocks.AIR);
			test.assertTrue(player.teleportTo(remote, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
				java.util.Set.of(), 0, 0, true), "Simulated player must reach remote interaction dimension");
			var result = click(player, pos);
			if (scenario == 0) {
				test.assertTrue(result == InteractionResult.SUCCESS, "Cross-dimension pairing must succeed");
				assertPair(test, a, b);
				test.assertTrue(stack.get(ModDataComponents.PORTAL_TARGET) == null, "Pairing clears selection");
				test.assertTrue(a.getBlockState().getValue(TeleporterBlock.POWERED) && !b.getBlockState().getValue(TeleporterBlock.POWERED), "Power must be sampled independently in each dimension");
				player.setShiftKeyDown(true);
				test.assertTrue(click(player, pos) == InteractionResult.SUCCESS, "Remote sneak click must unlink pair");
				assertUnlinked(test, a);
				assertUnlinked(test, b);
			} else if (scenario == 1) {
				test.assertTrue(result == InteractionResult.FAIL, "Occupied remote portal must reject pairing");
				assertPair(test, c, b);
				assertUnlinked(test, a);
				test.assertTrue(selection.equals(stack.get(ModDataComponents.PORTAL_TARGET)), "Rejected destination must retain selection");
			} else {
				test.assertTrue(result == InteractionResult.FAIL, "Missing source in another dimension must reject pairing");
				test.assertTrue(stack.get(ModDataComponents.PORTAL_TARGET) == null, "Obsolete selection must be cleared");
				assertUnlinked(test, b);
			}
			test.succeed();
		} finally {
			test.getLevel().getServer().getPlayerList().remove(player);
			player.discard();
			remote.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		}
	}

	private static InteractionResult click(Player player, BlockPos pos) {
		return ModItems.TUNER.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
			new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)));
	}
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
