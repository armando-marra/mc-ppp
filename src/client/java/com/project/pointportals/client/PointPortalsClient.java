package com.project.pointportals.client;

import com.project.pointportals.client.render.TeleporterRenderer;
import com.project.pointportals.registry.ModBlockEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class PointPortalsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRenderers.register(ModBlockEntities.TELEPORTER, TeleporterRenderer::new);
	}
}
