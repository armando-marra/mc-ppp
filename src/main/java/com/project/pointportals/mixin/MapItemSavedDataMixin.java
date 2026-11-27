package com.project.pointportals.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.project.pointportals.cartography.PortalMapMarker;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapItemSavedData.class)
abstract class MapItemSavedDataMixin extends SavedData implements PortalMapMarker.Storage {
	@Unique
	private final Map<BlockPos, PortalMapMarker> pointportals$markers = new LinkedHashMap<>();

	@Shadow
	protected abstract void addDecoration(Holder<MapDecorationType> type, @Nullable LevelAccessor level,
		String key, double x, double z, double rotation, @Nullable Component name);

	@Shadow
	protected abstract void removeDecoration(String key);

	@Override
	public Collection<PortalMapMarker> pointportals$markers() {
		return List.copyOf(pointportals$markers.values());
	}

	@Override
	public boolean pointportals$hasMarker(BlockPos pos) {
		return pointportals$markers.containsKey(pos);
	}

	@Override
	public void pointportals$putMarker(PortalMapMarker marker) {
		if (marker.equals(pointportals$markers.put(marker.pos(), marker))) return;
		var map = (MapItemSavedData) (Object) this;
		// Vanilla leaves a one-pixel margin for decorations relative to the terrain.
		int radius = 63 << map.scale;
		addDecoration(PortalMapMarker.type(), null, "pointportals:" + marker.pos().asLong(),
			Mth.clamp(marker.pos().getX() + 0.5, map.centerX - radius, map.centerX + radius),
			Mth.clamp(marker.pos().getZ() + 0.5, map.centerZ - radius, map.centerZ + radius), 0,
			marker.label().isEmpty() ? null : Component.literal(marker.label()));
		setDirty();
	}

	@Override
	public void pointportals$removeMarker(BlockPos pos) {
		if (pointportals$markers.remove(pos) != null) {
			removeDecoration("pointportals:" + pos.asLong());
			setDirty();
		}
	}

	@Inject(method = {"locked", "scaled"}, at = @At("RETURN"))
	private void pointportals$copyMarkers(CallbackInfoReturnable<MapItemSavedData> callback) {
		var result = callback.getReturnValue();
		var storage = PortalMapMarker.storage(result);
		for (var marker : pointportals$markers.values()) {
			if (result.locked || PortalMapMarker.covers(result, marker.pos())) {
				storage.pointportals$putMarker(marker);
			}
		}
	}

	@ModifyArg(method = "type", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/saveddata/SavedDataType;<init>(Lnet/minecraft/resources/Identifier;Ljava/util/function/Supplier;Lcom/mojang/serialization/Codec;Lnet/minecraft/util/datafix/DataFixTypes;)V"), index = 2)
	private static Codec<MapItemSavedData> pointportals$saveMarkers(Codec<MapItemSavedData> vanilla) {
		return RecordCodecBuilder.create(instance -> instance.group(
			MapCodec.assumeMapUnsafe(vanilla).forGetter(data -> data),
			PortalMapMarker.CODEC.listOf().optionalFieldOf("pointportals_portals", List.of())
				.forGetter(data -> List.copyOf(PortalMapMarker.storage(data).pointportals$markers()))
		).apply(instance, (data, markers) -> {
			markers.forEach(PortalMapMarker.storage(data)::pointportals$putMarker);
			return data;
		}));
	}
}
