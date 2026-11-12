package com.project.pointportals.registry;

import com.project.pointportals.PointPortals;
import com.project.pointportals.item.TunerItem;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;

public final class ModItems {
	public static final BlockItem TELEPORTER = registerBlockItem("teleporter", ModBlocks.TELEPORTER);

	public static final TunerItem TUNER = registerItem(
		"tuner",
		TunerItem::new,
		new Item.Properties()
			.stacksTo(1)
			.rarity(Rarity.RARE)
	);

	public static BlockItem registerBlockItem(String name, Block block) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, PointPortals.id(name));
		BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(key).useBlockDescriptionPrefix());
		return Registry.register(BuiltInRegistries.ITEM, key, blockItem);
	}

	public static <T extends Item> T registerItem(String name, Function<Item.Properties, T> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, PointPortals.id(name));
		T item = factory.apply(properties.setId(key));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> {
			output.accept(TELEPORTER);
		});

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
			output.accept(TUNER);
		});
	}

	private ModItems() {}
}
