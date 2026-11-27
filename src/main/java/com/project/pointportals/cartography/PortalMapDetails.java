package com.project.pointportals.cartography;

import com.project.pointportals.PointPortals;
import java.util.List;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public record PortalMapDetails(List<Component> pages) implements CustomPacketPayload {
	public static final Type<PortalMapDetails> TYPE = new Type<>(PointPortals.id("portal_map_details"));
	private static final StreamCodec<RegistryFriendlyByteBuf, PortalMapDetails> CODEC =
		ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list(MapItemSavedData.TRACKED_DECORATION_LIMIT))
			.map(PortalMapDetails::new, PortalMapDetails::pages);

	public static void initialize() {
		PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
		UseItemCallback.EVENT.register((player, level, hand) -> {
			var stack = player.getItemInHand(hand);
			if (player.isSpectator() || !player.isSecondaryUseActive() || !stack.is(Items.FILLED_MAP)) {
				return InteractionResult.PASS;
			}
			if (player instanceof ServerPlayer serverPlayer && ServerPlayNetworking.canSend(serverPlayer, TYPE)) {
				var server = serverPlayer.level();
				var map = MapItem.getSavedData(stack, server);
				var pages = map == null ? List.<Component>of() : PortalMapIndex.pairs(server, map);
				if (pages.isEmpty()) {
					pages = List.of(Component.translatable("cartography.pointportals.no_pairs"));
				}
				ServerPlayNetworking.send(serverPlayer, new PortalMapDetails(pages));
			}
			return InteractionResult.SUCCESS;
		});
	}

	@Override
	public Type<PortalMapDetails> type() {
		return TYPE;
	}
}
