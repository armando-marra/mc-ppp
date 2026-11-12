package com.project.pointportals.block.entity;

import com.project.pointportals.block.TeleporterBlock;
import com.project.pointportals.component.PortalTarget;
import com.project.pointportals.registry.ModBlockEntities;
import com.project.pointportals.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * BlockEntity managing persistent twin link data and synchronization for Teleporter blocks.
 */
public class TeleporterBlockEntity extends BlockEntity {
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
			twinLevel.getChunk(this.twinTarget.pos());
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
				if (be instanceof TeleporterBlockEntity twinEntity) {
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
	 * Server-side tick to periodically verify twin link integrity.
	 */
	public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, TeleporterBlockEntity entity) {
		if (!state.getValue(TeleporterBlock.LINKED) || entity.twinTarget == null) {
			return;
		}
		// Periodically verify link every 40 ticks (2 seconds)
		if (level.getGameTime() % 40 == 0) {
			entity.verifyAndSyncLink(false);
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
