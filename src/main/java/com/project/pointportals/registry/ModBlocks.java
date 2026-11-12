package com.project.pointportals.registry;

import com.project.pointportals.PointPortals;
import com.project.pointportals.block.TeleporterBlock;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {
	public static final Block TELEPORTER = registerBlock(
		"teleporter",
		TeleporterBlock::new,
		BlockBehaviour.Properties.of()
			.strength(4.0F, 9.0F)
			.sound(SoundType.STONE)
			.requiresCorrectToolForDrops()
			.lightLevel(state -> {
				if (state.getValue(TeleporterBlock.LINKED)) {
					return state.getValue(TeleporterBlock.POWERED) ? 12 : 3;
				}
				return 0;
			})
	);

	public static <T extends Block> T registerBlock(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, PointPortals.id(name));
		T block = factory.apply(properties.setId(key));
		return Registry.register(BuiltInRegistries.BLOCK, key, block);
	}

	public static void initialize() {
		// Triggers static initialization
	}

	private ModBlocks() {}
}
