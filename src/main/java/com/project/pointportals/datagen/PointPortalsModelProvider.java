package com.project.pointportals.datagen;

import com.google.gson.JsonObject;
import com.project.pointportals.PointPortals;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

/**
 * Programmatic Data Provider for Point-to-Point Portals blockstates, models, and modern item definitions.
 */
public class PointPortalsModelProvider implements DataProvider {
	private final FabricPackOutput packOutput;

	public PointPortalsModelProvider(FabricPackOutput packOutput) {
		this.packOutput = packOutput;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		PackOutput.PathProvider blockstates = this.packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
		PackOutput.PathProvider blockModels = this.packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/block");
		PackOutput.PathProvider itemModels = this.packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
		PackOutput.PathProvider items = this.packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");

		List<CompletableFuture<?>> futures = new ArrayList<>();

		// 1. Blockstate: all 4 combinations of LINKED and POWERED
		JsonObject blockstateJson = new JsonObject();
		JsonObject variants = new JsonObject();

		JsonObject inactiveVariant = new JsonObject();
		inactiveVariant.addProperty("model", "pointportals:block/teleporter_inactive");

		JsonObject standbyVariant = new JsonObject();
		standbyVariant.addProperty("model", "pointportals:block/teleporter_standby");

		JsonObject activeVariant = new JsonObject();
		activeVariant.addProperty("model", "pointportals:block/teleporter_active");

		variants.add("linked=false,powered=false", inactiveVariant);
		variants.add("linked=false,powered=true", inactiveVariant);
		variants.add("linked=true,powered=false", standbyVariant);
		variants.add("linked=true,powered=true", activeVariant);

		blockstateJson.add("variants", variants);
		futures.add(DataProvider.saveStable(output, blockstateJson, blockstates.json(PointPortals.id("teleporter"))));

		// 2. Block Models (Inactive, Standby, Active)
		futures.add(DataProvider.saveStable(output, createCubeBottomTopModel(
			"minecraft:block/respawn_anchor_top_off",
			"minecraft:block/obsidian",
			"minecraft:block/chiseled_copper"
		), blockModels.json(PointPortals.id("teleporter_inactive"))));

		futures.add(DataProvider.saveStable(output, createCubeBottomTopModel(
			"minecraft:block/crying_obsidian",
			"minecraft:block/obsidian",
			"minecraft:block/chiseled_copper"
		), blockModels.json(PointPortals.id("teleporter_standby"))));

		futures.add(DataProvider.saveStable(output, createCubeBottomTopModel(
			"minecraft:block/respawn_anchor_top",
			"minecraft:block/obsidian",
			"minecraft:block/chiseled_copper"
		), blockModels.json(PointPortals.id("teleporter_active"))));

		// 3. Item Models
		JsonObject teleporterItemModel = new JsonObject();
		teleporterItemModel.addProperty("parent", "pointportals:block/teleporter_inactive");
		futures.add(DataProvider.saveStable(output, teleporterItemModel, itemModels.json(PointPortals.id("teleporter"))));

		JsonObject tunerItemModel = new JsonObject();
		tunerItemModel.addProperty("parent", "minecraft:item/generated");
		JsonObject tunerTextures = new JsonObject();
		tunerTextures.addProperty("layer0", "minecraft:item/amethyst_shard");
		tunerItemModel.add("textures", tunerTextures);
		futures.add(DataProvider.saveStable(output, tunerItemModel, itemModels.json(PointPortals.id("tuner"))));

		// 4. Modern Item Definitions (Minecraft 26.2+ client item registry format)
		JsonObject teleporterItemDef = createItemDefinition("pointportals:item/teleporter");
		futures.add(DataProvider.saveStable(output, teleporterItemDef, items.json(PointPortals.id("teleporter"))));

		JsonObject tunerItemDef = createItemDefinition("pointportals:item/tuner");
		futures.add(DataProvider.saveStable(output, tunerItemDef, items.json(PointPortals.id("tuner"))));

		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}

	private static JsonObject createCubeBottomTopModel(String top, String bottom, String side) {
		JsonObject model = new JsonObject();
		model.addProperty("parent", "minecraft:block/cube_bottom_top");
		JsonObject textures = new JsonObject();
		textures.addProperty("top", top);
		textures.addProperty("bottom", bottom);
		textures.addProperty("side", side);
		model.add("textures", textures);
		return model;
	}

	private static JsonObject createItemDefinition(String modelId) {
		JsonObject itemDef = new JsonObject();
		JsonObject model = new JsonObject();
		model.addProperty("type", "minecraft:model");
		model.addProperty("model", modelId);
		itemDef.add("model", model);
		return itemDef;
	}

	@Override
	public String getName() {
		return "PointPortals Model and Item Definitions";
	}
}
