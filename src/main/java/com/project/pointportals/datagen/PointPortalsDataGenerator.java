package com.project.pointportals.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class PointPortalsDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(PointPortalsBlockTagProvider::new);
		pack.addProvider(PointPortalsBlockLootTableProvider::new);
		pack.addProvider(PointPortalsRecipeProvider::new);
		pack.addProvider(PointPortalsEnglishLangProvider::new);
		pack.addProvider(PointPortalsModelProvider::new);
	}
}
