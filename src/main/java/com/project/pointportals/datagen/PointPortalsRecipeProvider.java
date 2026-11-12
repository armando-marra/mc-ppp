package com.project.pointportals.datagen;

import com.project.pointportals.PointPortals;
import com.project.pointportals.registry.ModBlocks;
import com.project.pointportals.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

public class PointPortalsRecipeProvider extends FabricRecipeProvider {
	public PointPortalsRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
		return new RecipeProvider(registries, output) {
			@Override
			public void buildRecipes() {
				ResourceKey<Recipe<?>> teleporterRecipeKey = ResourceKey.create(Registries.RECIPE, PointPortals.id("teleporter"));
				ResourceKey<Recipe<?>> tunerRecipeKey = ResourceKey.create(Registries.RECIPE, PointPortals.id("tuner"));

				// 2x Teleporter Blocks: Obsidian corners, Copper edges, Redstone sides, Eye of Ender center
				this.shaped(RecipeCategory.TRANSPORTATION, ModBlocks.TELEPORTER, 2)
					.pattern("OCO")
					.pattern("RER")
					.pattern("OCO")
					.define('O', Items.OBSIDIAN)
					.define('C', Items.COPPER_INGOT)
					.define('R', Items.REDSTONE)
					.define('E', Items.ENDER_EYE)
					.unlockedBy("has_ender_eye", this.has(Items.ENDER_EYE))
					.save(this.output, teleporterRecipeKey);

				// Phase Tuner: Amethyst shards top, Ender pearl center, Blaze rod handle
				this.shaped(RecipeCategory.TOOLS, ModItems.TUNER, 1)
					.pattern(" A ")
					.pattern(" P ")
					.pattern(" B ")
					.define('A', Items.AMETHYST_SHARD)
					.define('P', Items.ENDER_PEARL)
					.define('B', Items.BLAZE_ROD)
					.unlockedBy("has_ender_pearl", this.has(Items.ENDER_PEARL))
					.save(this.output, tunerRecipeKey);
			}
		};
	}

	@Override
	public String getName() {
		return "PointPortals Recipes";
	}
}
