package com.project.pointportals.cartography;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.project.pointportals.PointPortals;
import java.util.Collection;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public record PortalMapMarker(BlockPos pos, UUID portalId, String label) {
	public static final Codec<PortalMapMarker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		BlockPos.CODEC.fieldOf("pos").forGetter(PortalMapMarker::pos),
		UUIDUtil.CODEC.fieldOf("portal").forGetter(PortalMapMarker::portalId),
		Codec.STRING.optionalFieldOf("label", "").forGetter(PortalMapMarker::label)
	).apply(instance, PortalMapMarker::new));

	public static final ResourceKey<MapDecorationType> TYPE =
		ResourceKey.create(Registries.MAP_DECORATION_TYPE, PointPortals.id("portal"));

	public static void initialize() {
		Registry.register(BuiltInRegistries.MAP_DECORATION_TYPE, TYPE,
			new MapDecorationType(TYPE.identifier(), true, MapDecorationType.NO_MAP_COLOR, false, true));
	}

	public static Holder<MapDecorationType> type() {
		return BuiltInRegistries.MAP_DECORATION_TYPE.getOrThrow(TYPE);
	}

	public static boolean covers(MapItemSavedData map, BlockPos pos) {
		int radius = 64 << map.scale;
		return pos.getX() >= map.centerX - radius && pos.getX() < map.centerX + radius
			&& pos.getZ() >= map.centerZ - radius && pos.getZ() < map.centerZ + radius;
	}

	public static Storage storage(MapItemSavedData map) {
		return (Storage) map;
	}

	public interface Storage {
		Collection<PortalMapMarker> pointportals$markers();

		boolean pointportals$hasMarker(BlockPos pos);

		void pointportals$putMarker(PortalMapMarker marker);

		void pointportals$removeMarker(BlockPos pos);
	}
}
