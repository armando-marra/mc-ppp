package com.project.pointportals.datagen;

import com.project.pointportals.PointPortals;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;

public class PointPortalsBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
	public PointPortalsBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		ResourceKey<Block> teleporterKey = ResourceKey.create(Registries.BLOCK, PointPortals.id("teleporter"));
		this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
			.add(teleporterKey);
		this.tag(BlockTags.NEEDS_IRON_TOOL)
			.add(teleporterKey);
	}
}
