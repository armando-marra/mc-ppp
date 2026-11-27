package com.project.pointportals.client;

import com.project.pointportals.cartography.PortalMapDetails;
import com.project.pointportals.client.render.TeleporterRenderer;
import com.project.pointportals.registry.ModBlockEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class PointPortalsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PortalGuide.initialize();
		BlockEntityRenderers.register(ModBlockEntities.TELEPORTER, TeleporterRenderer::new);
		ClientPlayNetworking.registerGlobalReceiver(PortalMapDetails.TYPE, (details, context) ->
			context.client().gui.setScreen(new BookViewScreen(new BookViewScreen.BookAccess(details.pages()))));
	}
}
