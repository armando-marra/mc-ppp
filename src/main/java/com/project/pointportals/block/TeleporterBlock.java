package com.project.pointportals.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * The core Point-to-Point Teleporter block.
 * Maintains LINKED (paired with twin) and POWERED (Redstone activated) blockstates.
 */
public class TeleporterBlock extends Block {
	public static final BooleanProperty LINKED = BooleanProperty.create("linked");
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public TeleporterBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(LINKED, false)
			.setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LINKED, POWERED);
	}
}
