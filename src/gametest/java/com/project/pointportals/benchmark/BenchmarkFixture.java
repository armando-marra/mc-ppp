package com.project.pointportals.benchmark;

import com.project.pointportals.block.TeleporterBlock;
import com.project.pointportals.block.entity.TeleporterBlockEntity;
import com.project.pointportals.component.PortalTarget;
import com.project.pointportals.registry.ModBlocks;
import com.project.pointportals.registry.ModDataComponents;
import com.project.pointportals.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Disposable, widely separated targets; no target is reused between measured operations. */
final class BenchmarkFixture implements AutoCloseable {
	final ServerLevel source;
	final ServerLevel remote;
	final BlockPos local;
	final BlockPos target;
	final String operation;
	final String state;
	final Player player;
	final ItemStack tuner = new ItemStack(ModItems.TUNER);
	final TeleporterBlockEntity portal;
	Entity pig;
	boolean exercised;
	boolean closed;

	BenchmarkFixture(GameTestHelper test, ServerLevel remote, Player player, String operation,
		String state, int index, int unique) {
		this.source = test.getLevel();
		this.remote = remote;
		this.player = player;
		this.operation = operation;
		this.state = state;
		local = test.absolutePos(new BlockPos(2 + (index % 8) * 3, 2, 2 + (index / 8) * 3));
		target = new BlockPos(1_000_008 + unique * 512, 240, 1_000_008);
		source.getChunk(local);
		source.getChunkSource().addTicketWithRadius(TicketType.FORCED, new ChunkPos(local.getX() >> 4, local.getZ() >> 4), 2);
		source.setBlock(local, ModBlocks.TELEPORTER.defaultBlockState(), Block.UPDATE_ALL);
		source.setBlock(local.below(), Blocks.REDSTONE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
		portal = (TeleporterBlockEntity) source.getBlockEntity(local);
		if (!state.equals("new")) {
			remote.getChunk(target);
			for (var pos : BlockPos.betweenClosed(target.offset(-1, 1, -1), target.offset(1, 4, 1))) {
				remote.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			}
			remote.setBlock(target, ModBlocks.TELEPORTER.defaultBlockState(), Block.UPDATE_ALL);
			if (state.equals("loaded")) {
				remote.getChunkSource().addTicketWithRadius(TicketType.FORCED, new ChunkPos(target.getX() >> 4, target.getZ() >> 4), 0);
			}
		}
		if (operation.equals("tuner")) {
			if (state.equals("new")) {
				tuner.set(ModDataComponents.PORTAL_TARGET, PortalTarget.of(remote, target));
			} else {
				test.assertTrue(click(remote, target) == InteractionResult.SUCCESS, "Real first selection must succeed");
			}
		} else {
			portal.setTwinTarget(PortalTarget.of(remote, target));
			if (!state.equals("new")) {
				var twin = (TeleporterBlockEntity) remote.getBlockEntity(target);
				twin.setTwinTarget(PortalTarget.of(source, local));
				remote.setBlock(target, twin.getBlockState().setValue(TeleporterBlock.LINKED, true), Block.UPDATE_ALL);
			}
			source.setBlock(local, portal.getBlockState().setValue(TeleporterBlock.LINKED, true), Block.UPDATE_ALL);
		}
	}

	void prepareEntity(GameTestHelper test) {
		if (operation.equals("teleport")) {
			if (pig == null) {
				pig = EntityTypes.PIG.spawn(source, local.offset(1, 1, 1), EntitySpawnReason.COMMAND);
				test.assertTrue(pig != null, "Benchmark entity must spawn");
				((net.minecraft.world.entity.Mob) pig).setNoAi(true);
				pig.setNoGravity(true);
				pig.setPortalCooldown(0);
				pig.setYRot(37.0F);
				pig.setXRot(12.0F);
			}
			test.assertTrue(portal.getBlockState().getValue(TeleporterBlock.LINKED), "Source must be linked before measurement");
			test.assertTrue(portal.getBlockState().getValue(TeleporterBlock.POWERED), "Source must be powered before measurement");
		}
	}

	boolean exercise() {
		exercised = true;
		return operation.equals("tuner") ? click(source, local) == InteractionResult.SUCCESS
			: portal.teleportEntity(source, local, portal.getBlockState(), pig);
	}

	boolean arrivalReady() {
		if (!operation.equals("teleport") || state.equals("new")) return true;
		return !remote.getEntities((Entity) null, new AABB(target).inflate(3), e -> e.getUUID().equals(pig.getUUID())).isEmpty();
	}

	void verify(GameTestHelper test, boolean success) {
		test.assertTrue(success != state.equals("new"), "Only new/missing targets must be rejected");
		if (state.equals("new")) {
			test.assertFalse(portal.isLinked(), "Missing destination must leave the source unlinked");
			test.assertFalse(portal.getBlockState().getValue(TeleporterBlock.LINKED), "Missing target must reset LINKED state");
			test.assertFalse(tuner.has(ModDataComponents.PORTAL_TARGET), "Missing source must clear tuner selection");
			return;
		}
		test.assertTrue(portal.verifyAndSyncLink(false), "Measured operation must preserve reciprocal pairing");
		var twin = (TeleporterBlockEntity) remote.getBlockEntity(target);
		test.assertTrue(twin != null && twin.verifyAndSyncLink(false), "Twin must point back");
		if (operation.equals("teleport")) {
			var uuid = pig.getUUID();
			var arrivals = remote.getEntities((Entity) null, new AABB(target).inflate(3), e -> e.getUUID().equals(uuid));
			test.assertTrue(arrivals.size() == 1, "Exactly one arrival must be tracked at " + target
				+ "; matches=" + arrivals.size() + "; sourceRemoved=" + pig.isRemoved()
				+ "; destinationEntity=" + remote.getEntity(uuid)
				+ "; entityTicking=" + remote.isPositionEntityTicking(target));
			test.assertTrue(source.getEntities((Entity) null, new AABB(local).inflate(3), e -> e.getUUID().equals(uuid)).isEmpty(),
				"Departure must not retain a duplicate");
			pig = arrivals.getFirst();
			test.assertTrue(Math.abs(pig.getX() - target.getX() - 0.5) < 0.01
				&& Math.abs(pig.getY() - target.getY() - 1.0) < 0.01
				&& Math.abs(pig.getZ() - target.getZ() - 0.5) < 0.01, "Arrival must use exact coordinates");
			test.assertTrue(Math.abs(pig.getYRot() - 37.0F) < 0.01 && Math.abs(pig.getXRot() - 12.0F) < 0.01,
				"Arrival must preserve rotation");
			test.assertTrue(pig.getPortalCooldown() >= 40, "Arrival cooldown must remain active");
			test.assertFalse(twin.teleportEntity(remote, target, twin.getBlockState(), pig), "Immediate return must be rejected");
		}
	}

	private InteractionResult click(ServerLevel level, BlockPos pos) {
		return ModItems.TUNER.useOn(new ClickContext(level, player, tuner, pos));
	}

	private static final class ClickContext extends UseOnContext {
		ClickContext(ServerLevel level, Player player, ItemStack stack, BlockPos pos) {
			super(level, player, InteractionHand.MAIN_HAND, stack,
				new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
		}
	}

	@Override
	public void close() {
		if (closed) return;
		closed = true;
		if (pig != null) pig.discard();
		source.setBlock(local, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		source.setBlock(local.below(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		source.getChunkSource().removeTicketWithRadius(TicketType.FORCED, new ChunkPos(local.getX() >> 4, local.getZ() >> 4), 2);
		if (!state.equals("new") || exercised) {
			remote.getChunk(target);
			remote.setBlock(target, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		}
		if (state.equals("loaded")) {
			remote.getChunkSource().removeTicketWithRadius(TicketType.FORCED, new ChunkPos(target.getX() >> 4, target.getZ() >> 4), 0);
		}
	}
}
