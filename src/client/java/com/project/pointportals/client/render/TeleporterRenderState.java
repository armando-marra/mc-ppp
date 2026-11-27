package com.project.pointportals.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

/** Immutable-to-the-submission-stage snapshot; no world access during drawing. */
public final class TeleporterRenderState extends BlockEntityRenderState {
	public boolean active;
	public String label = "";
	public float yaw;
	public float u0;
	public float u1;
	public float v0;
	public float v1;
}
