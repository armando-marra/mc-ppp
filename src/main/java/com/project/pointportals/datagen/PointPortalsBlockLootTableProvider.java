package com.project.pointportals.datagen;

import com.project.pointportals.registry.ModBlocks;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;

public class PointPortalsBlockLootTableProvider extends FabricBlockLootSubProvider {
	public PointPortalsBlockLootTableProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	public void generate() {
		this.dropSelf(ModBlocks.TELEPORTER);
	}
}
