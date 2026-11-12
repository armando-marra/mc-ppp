package com.project.pointportals.registry;

import com.project.pointportals.PointPortals;
import com.project.pointportals.block.entity.TeleporterBlockEntity;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
	public static final BlockEntityType<TeleporterBlockEntity> TELEPORTER = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		PointPortals.id("teleporter"),
		new BlockEntityType<>(TeleporterBlockEntity::new, Set.of(ModBlocks.TELEPORTER))
	);

	public static void initialize() {
		// Triggers static initialization
	}

	private ModBlockEntities() {}
}
