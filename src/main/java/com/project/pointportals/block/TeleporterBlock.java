package com.project.pointportals.block;

import com.project.pointportals.block.entity.TeleporterBlockEntity;
import com.project.pointportals.component.PortalTarget;
import com.project.pointportals.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * The core Point-to-Point Teleporter block.
 * Maintains LINKED (paired with twin) and POWERED (Redstone activated) blockstates.
 */
public class TeleporterBlock extends Block implements EntityBlock {
	public static final BooleanProperty LINKED = BooleanProperty.create("linked");
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public TeleporterBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(LINKED, false)
			.setValue(POWERED, false));
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
		return new TeleporterBlockEntity(worldPosition, blockState);
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
		Level level,
		BlockState state,
		BlockEntityType<T> blockEntityType
	) {
		if (level.isClientSide()) {
			return null;
		}
		if (blockEntityType == ModBlockEntities.TELEPORTER) {
			return (lvl, pos, st, be) -> {
				if (be instanceof TeleporterBlockEntity teleporter && lvl instanceof ServerLevel serverLevel) {
					TeleporterBlockEntity.serverTick(serverLevel, pos, st, teleporter);
				}
			};
		}
		return null;
	}

	@Override
	protected void neighborChanged(
		BlockState state,
		Level level,
		BlockPos pos,
		Block neighborBlock,
		@Nullable Orientation orientation,
		boolean movedByPiston
	) {
		super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
		if (level.isClientSide()) {
			return;
		}

		boolean hasSignal = level.hasNeighborSignal(pos);
		boolean currentPowered = state.getValue(POWERED);
		if (hasSignal != currentPowered) {
			BlockState newState = state.setValue(POWERED, hasSignal);
			level.setBlock(pos, newState, Block.UPDATE_ALL);

			// Audio feedback when transitioning between Standby and Active
			if (state.getValue(LINKED)) {
				if (hasSignal) {
					level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.7F, 1.2F);
				} else {
					level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.7F, 1.0F);
				}
			}
		}

		// Verify twin link integrity if linked
		if (state.getValue(LINKED)) {
			BlockEntity be = level.getBlockEntity(pos);
			if (be instanceof TeleporterBlockEntity teleporter) {
				teleporter.verifyAndSyncLink(false);
			}
		}
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);
		if (!oldState.is(state.getBlock()) && !level.isClientSide()) {
			boolean hasSignal = level.hasNeighborSignal(pos);
			if (hasSignal != state.getValue(POWERED)) {
				level.setBlock(pos, state.setValue(POWERED, hasSignal), Block.UPDATE_ALL);
			}
		}
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		super.stepOn(level, pos, state, entity);
		if (level.isClientSide() || entity.isPassenger() || entity.isVehicle()) {
			return;
		}

		if (!state.getValue(LINKED) || !state.getValue(POWERED)) {
			return;
		}

		if (entity.isOnPortalCooldown()) {
			entity.setPortalCooldown(40);
			return;
		}

		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof TeleporterBlockEntity teleporter && level instanceof ServerLevel serverLevel) {
			teleporter.teleportEntity(serverLevel, pos, state, entity);
		}
	}

	@Override
	protected InteractionResult useWithoutItem(
		BlockState state,
		Level level,
		BlockPos pos,
		Player player,
		BlockHitResult hitResult
	) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof TeleporterBlockEntity teleporter) {
			teleporter.verifyAndSyncLink(false);
			PortalTarget target = teleporter.getTwinTarget();
			if (target != null && state.getValue(LINKED)) {
				String dimName = target.dimension().identifier().toString();
				BlockPos tPos = target.pos();
				if (state.getValue(POWERED)) {
					player.sendOverlayMessage(
						Component.translatable("message.pointportals.status_active", dimName, tPos.getX(), tPos.getY(), tPos.getZ())
							.withStyle(ChatFormatting.GREEN)
					);
				} else {
					player.sendOverlayMessage(
						Component.translatable("message.pointportals.status_standby", dimName, tPos.getX(), tPos.getY(), tPos.getZ())
							.withStyle(ChatFormatting.YELLOW)
					);
				}
			} else {
				player.sendOverlayMessage(
					Component.translatable("message.pointportals.status_unlinked")
						.withStyle(ChatFormatting.GRAY)
				);
			}
			return InteractionResult.SUCCESS;
		}

		return super.useWithoutItem(state, level, pos, player, hitResult);
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		if (!state.getValue(LINKED)) {
			return 0;
		}
		return state.getValue(POWERED) ? 15 : 7;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LINKED)) {
			return;
		}

		double x = pos.getX() + 0.5;
		double y = pos.getY() + 1.0;
		double z = pos.getZ() + 0.5;

		if (state.getValue(POWERED)) {
			// Active: vibrant portal and reverse portal particles rising
			for (int i = 0; i < 2; i++) {
				double offsetX = (random.nextDouble() - 0.5) * 0.8;
				double offsetZ = (random.nextDouble() - 0.5) * 0.8;
				level.addParticle(
					ParticleTypes.PORTAL,
					x + offsetX,
					y,
					z + offsetZ,
					(random.nextDouble() - 0.5) * 0.2,
					(random.nextDouble() * 0.5) + 0.1,
					(random.nextDouble() - 0.5) * 0.2
				);
				level.addParticle(
					ParticleTypes.REVERSE_PORTAL,
					x + offsetX,
					y,
					z + offsetZ,
					0.0,
					0.05,
					0.0
				);
			}
			if (random.nextInt(120) == 0) {
				level.playLocalSound(x, y, z, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.25F, 1.2F, false);
			}
		} else {
			// Standby: faint enchanting particles indicating armed connection
			if (random.nextInt(4) == 0) {
				double offsetX = (random.nextDouble() - 0.5) * 0.6;
				double offsetZ = (random.nextDouble() - 0.5) * 0.6;
				level.addParticle(
					ParticleTypes.ENCHANT,
					x + offsetX,
					y,
					z + offsetZ,
					0.0,
					0.1,
					0.0
				);
			}
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LINKED, POWERED);
	}
}
