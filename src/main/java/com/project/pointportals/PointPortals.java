package com.project.pointportals;

import com.project.pointportals.registry.ModBlockEntities;
import com.project.pointportals.registry.ModBlocks;
import com.project.pointportals.registry.ModDataComponents;
import com.project.pointportals.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PointPortals implements ModInitializer {
	public static final String MOD_ID = "pointportals";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Point-to-Point Portals mod");

		ModDataComponents.initialize();
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModItems.initialize();

		LOGGER.info("Point-to-Point Portals registration complete");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
