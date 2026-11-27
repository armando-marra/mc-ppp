package com.project.pointportals.mixin;

import com.project.pointportals.cartography.PortalMapIndex;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
abstract class ServerLevelMixin {
	@Inject(method = "getMapData", at = @At("RETURN"))
	private void pointportals$refreshMap(MapId id, CallbackInfoReturnable<MapItemSavedData> callback) {
		var map = callback.getReturnValue();
		if (map != null) PortalMapIndex.refresh((ServerLevel) (Object) this, map);
	}
}
