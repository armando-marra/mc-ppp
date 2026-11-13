package com.project.pointportals.block.entity;

import com.project.pointportals.block.TeleporterBlock;
import com.project.pointportals.component.PortalTarget;
import com.project.pointportals.diagnostics.PortalChunkLoads;
import com.project.pointportals.registry.ModBlockEntities;
import com.project.pointportals.registry.ModBlocks;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * BlockEntity managing persistent twin link data, synchronization, and teleportation for Teleporter blocks.
 */
public class TeleporterBlockEntity extends BlockEntity {
	private static final int LINK_CHECK_INTERVAL = 40;
	private @Nullable PortalTarget twinTarget;

	public TeleporterBlockEntity(BlockPos worldPosition, BlockState blockState) {
		super(ModBlockEntities.TELEPORTER, worldPosition, blockState);
	}

	public boolean isLinked() {
		return this.twinTarget != null;
	}

	public @Nullable PortalTarget getTwinTarget() {
		return this.twinTarget;
	}

	public void setTwinTarget(@Nullable PortalTarget target) {
		this.twinTarget = target;
		this.setChanged();
		if (this.level != null && !this.level.isClientSide()) {
			this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
		}
	}

	/**
	 * Clears the stored twin destination and resets the blockstate to unlinked.
	 */
	public void clearAndReset(boolean sync) {
		boolean wasLinked = this.twinTarget != null;
		this.twinTarget = null;
		this.setChanged();

		if (this.level != null && !this.level.isClientSide()) {
			BlockState state = this.getBlockState();
			if (state.is(ModBlocks.TELEPORTER)) {
				boolean wasPowered = state.getValue(TeleporterBlock.POWERED);
				boolean hasSignal = this.level.hasNeighborSignal(this.worldPosition);
				this.level.setBlock(
					this.worldPosition,
					state.setValue(TeleporterBlock.LINKED, false).setValue(TeleporterBlock.POWERED, hasSignal),
					Block.UPDATE_ALL
				);
				if (wasLinked && wasPowered) {
					this.level.playSound(null, this.worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.7F, 1.0F);
				}
			}
			if (sync) {
				this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
			}
		}
	}

	/**
	 * Verifies whether the registered twin portal is still alive and reciprocally linked.
	 * If desynchronized or missing, cleanly resets this portal.
	 *
	 * @param allowChunkLoading whether to load the twin's chunk if currently unloaded
	 * @return true if the twin link is verified intact, false if broken or invalid
	 */
	public boolean verifyAndSyncLink(boolean allowChunkLoading) {
		if (this.level == null || this.level.isClientSide()) {
			return this.twinTarget != null;
		}

		if (this.twinTarget == null) {
			BlockState state = this.getBlockState();
			if (state.is(ModBlocks.TELEPORTER) && state.getValue(TeleporterBlock.LINKED)) {
				this.clearAndReset(true);
			}
			return false;
		}

		MinecraftServer server = this.level.getServer();
		if (server == null) {
			return false;
		}

		ServerLevel twinLevel = server.getLevel(this.twinTarget.dimension());
		if (twinLevel == null) {
			// Target dimension no longer exists
			this.clearAndReset(true);
			return false;
		}

		boolean isLoaded = twinLevel.isLoaded(this.twinTarget.pos());
		if (!isLoaded && !allowChunkLoading) {
			// Target chunk unloaded, retain link in standby
			return true;
		}

		if (!isLoaded && allowChunkLoading) {
			PortalChunkLoads.resolve(twinLevel, this.twinTarget.pos(), "link_verification");
		}

		BlockState twinState = twinLevel.getBlockState(this.twinTarget.pos());
		if (!twinState.is(ModBlocks.TELEPORTER)) {
			// Target block is not a Teleporter
			this.clearAndReset(true);
			return false;
		}

		BlockEntity be = twinLevel.getBlockEntity(this.twinTarget.pos());
		if (!(be instanceof TeleporterBlockEntity twinEntity)) {
			// Target is missing the required BlockEntity
			this.clearAndReset(true);
			return false;
		}

		PortalTarget remoteTarget = twinEntity.getTwinTarget();
		if (remoteTarget == null ||
			!remoteTarget.dimension().equals(this.level.dimension()) ||
			!remoteTarget.pos().equals(this.worldPosition)) {
			// Reciprocal link broken: target does not link back to us
			this.clearAndReset(true);
			return false;
		}

		return true;
	}

	/**
	 * Executes safe cross-dimensional teleportation for an entity standing on the portal.
	 */
	public boolean teleportEntity(ServerLevel sourceLevel, BlockPos sourcePos, BlockState sourceState, Entity entity) {
		if (entity.level().isClientSide() || entity.isPassenger() || entity.isVehicle()) {
			return false;
		}

		if (entity.isOnPortalCooldown()) {
			entity.setPortalCooldown(40);
			return false;
		}

		if (!sourceState.getValue(TeleporterBlock.LINKED) || !sourceState.getValue(TeleporterBlock.POWERED)) {
			return false;
		}

		if (this.twinTarget == null) {
			this.clearAndReset(true);
			return false;
		}

		MinecraftServer server = sourceLevel.getServer();
		if (server == null) {
			return false;
		}

		ServerLevel destLevel = server.getLevel(this.twinTarget.dimension());
		if (destLevel == null) {
			this.clearAndReset(true);
			return false;
		}

		BlockPos destPos = this.twinTarget.pos();

		// Safely resolve the destination chunk
		PortalChunkLoads.resolve(destLevel, destPos, "teleport_destination");

		BlockState destState = destLevel.getBlockState(destPos);
		if (!destState.is(ModBlocks.TELEPORTER)) {
			this.clearAndReset(true);
			return false;
		}

		BlockEntity destBe = destLevel.getBlockEntity(destPos);
		if (!(destBe instanceof TeleporterBlockEntity destTeleporter)) {
			this.clearAndReset(true);
			return false;
		}

		PortalTarget reciprocalTarget = destTeleporter.getTwinTarget();
		if (reciprocalTarget == null ||
			!reciprocalTarget.dimension().equals(sourceLevel.dimension()) ||
			!reciprocalTarget.pos().equals(sourcePos)) {
			this.clearAndReset(true);
			return false;
		}

		double destX = destPos.getX() + 0.5;
		double destY = destPos.getY() + 1.0;
		double destZ = destPos.getZ() + 0.5;
		AABB arrivalBox = entity.getBoundingBox().move(
			destX - entity.getX(), destY - entity.getY(), destZ - entity.getZ());

		// Check the entity's entire volume, including partial block collision shapes.
		if (!isSafeArrival(destLevel, entity, arrivalBox)) {
			sourceLevel.playSound(null, sourcePos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 0.6F);
			if (entity instanceof Player player) {
				player.sendOverlayMessage(
					Component.translatable("message.pointportals.destination_obstructed")
						.withStyle(ChatFormatting.RED)
				);
			}
			entity.setPortalCooldown(40);
			return false;
		}

		// Apply anti-loop cooldown (60 ticks / 3 seconds)
		entity.setPortalCooldown(60);

		// Departure effects
		sourceLevel.playSound(null, sourcePos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.2F);
		sourceLevel.sendParticles(ParticleTypes.PORTAL, sourcePos.getX() + 0.5, sourcePos.getY() + 1.0, sourcePos.getZ() + 0.5, 25, 0.3, 0.5, 0.3, 0.2);

		// Execute cross-dimensional transfer
		// Use vanilla's expiring portal ticket so an otherwise idle destination
		// dimension tracks and ticks the arriving entity after the transfer.
		boolean success = entity.teleport(new TeleportTransition(
			destLevel,
			new Vec3(destX, destY, destZ),
			Vec3.ZERO,
			entity.getYRot(),
			entity.getXRot(),
			TeleportTransition.PLACE_PORTAL_TICKET
		)) != null;

		if (success) {
			// Arrival effects at destination
			destLevel.playSound(null, destPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.2F);
			destLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, destX, destY, destZ, 25, 0.3, 0.5, 0.3, 0.1);
		}

		return success;
	}

	private static boolean isSafeArrival(ServerLevel level, Entity entity, AABB arrivalBox) {
		if (!level.getWorldBorder().isWithinBounds(arrivalBox) || !level.noCollision(entity, arrivalBox)) {
			return false;
		}
		// Deflate slightly so merely touching a neighbouring block is not rejected.
		AABB interior = arrivalBox.deflate(1.0E-7);
		for (BlockPos pos : BlockPos.betweenClosed(
			BlockPos.containing(interior.minX, interior.minY, interior.minZ),
			BlockPos.containing(interior.maxX, interior.maxY, interior.maxZ))) {
			BlockState state = level.getBlockState(pos);
			if (state.is(Blocks.LAVA) || state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Notifies the twin portal when this block entity is broken or destroyed.
	 */
	public void notifyTwinOfRemoval() {
		if (this.twinTarget == null || this.level == null || this.level.isClientSide()) {
			return;
		}

		MinecraftServer server = this.level.getServer();
		if (server == null) {
			return;
		}

		ServerLevel twinLevel = server.getLevel(this.twinTarget.dimension());
		if (twinLevel != null && twinLevel.isLoaded(this.twinTarget.pos())) {
			BlockState twinState = twinLevel.getBlockState(this.twinTarget.pos());
			if (twinState.is(ModBlocks.TELEPORTER)) {
				BlockEntity be = twinLevel.getBlockEntity(this.twinTarget.pos());
				if (be instanceof TeleporterBlockEntity twinEntity &&
					PortalTarget.of(this.level, this.worldPosition).equals(twinEntity.getTwinTarget())) {
					twinEntity.clearAndReset(true);
				}
			}
		}
	}

	/**
	 * Triggered only when the block itself is removed or replaced by another block.
	 * (Does NOT trigger on chunk unloads, keeping links safe across unloads).
	 */
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		if (this.level != null && !this.level.isClientSide()) {
			this.notifyTwinOfRemoval();
		}
	}

	/**
	 * Server-side tick to periodically verify twin link integrity and process standing entities.
	 */
	public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, TeleporterBlockEntity entity) {
		if (!state.getValue(TeleporterBlock.LINKED)) {
			return;
		}
		if (entity.twinTarget == null) {
			entity.clearAndReset(true);
			return;
		}

		// Spread verification work across ticks rather than checking every portal at once.
		if (Math.floorMod(level.getGameTime(), LINK_CHECK_INTERVAL) ==
			Math.floorMod(pos.hashCode(), LINK_CHECK_INTERVAL)) {
			if (!entity.verifyAndSyncLink(false)) {
				return;
			}
		}

		// Active teleporters monitor standing entities
		if (state.getValue(TeleporterBlock.POWERED)) {
			AABB triggerBox = new AABB(
				pos.getX(), pos.getY() + 0.9, pos.getZ(),
				pos.getX() + 1.0, pos.getY() + 1.5, pos.getZ() + 1.0
			);

			List<Entity> entities = level.getEntities((Entity) null, triggerBox);
			for (Entity e : entities) {
				if (e.isPassenger() || e.isVehicle()) {
					continue;
				}
				if (e.isOnPortalCooldown()) {
					e.setPortalCooldown(40);
					continue;
				}
				entity.teleportEntity(level, pos, state, e);
			}
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.storeNullable("twin_target", PortalTarget.CODEC, this.twinTarget);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.twinTarget = input.read("twin_target", PortalTarget.CODEC).orElse(null);
	}

	@Override
	public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return this.saveCustomOnly(registries);
	}
}
