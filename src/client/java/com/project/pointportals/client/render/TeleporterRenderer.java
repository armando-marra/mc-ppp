package com.project.pointportals.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.project.pointportals.block.TeleporterBlock;
import com.project.pointportals.block.entity.TeleporterBlockEntity;
import com.project.pointportals.registry.ModBlocks;
import java.util.Arrays;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** The live vanilla Nether portal sprite clipped to an upright ellipse. */
public final class TeleporterRenderer implements BlockEntityRenderer<TeleporterBlockEntity, TeleporterRenderState> {
	private static final SpriteId PORTAL = new SpriteId(Identifier.withDefaultNamespace("textures/atlas/blocks.png"),
		Identifier.withDefaultNamespace("block/nether_portal"));
	private static final int ROWS = 32;
	private static final float HALF_WIDTH = 0.65F;
	private static final float HALF_HEIGHT = 1.10F;
	private static final float[] OUTLINE_X = new float[ROWS * 2];
	private static final float[] OUTLINE_Y = new float[ROWS * 2];
	static {
		// Use precisely the same outline samples as the textured strips, avoiding gaps.
		for (int i = 0; i < ROWS * 2; i++) {
			float y = i < ROWS ? -1 + i * 2.0F / ROWS : 1 - (i - ROWS) * 2.0F / ROWS;
			OUTLINE_Y[i] = y;
			OUTLINE_X[i] = (i < ROWS ? 1 : -1) * (float) Math.sqrt(Math.max(0, 1 - y * y));
		}
	}
	// Distances from the crown along one half of the label baseline, in blocks.
	private static final float LABEL_RX = HALF_WIDTH * 1.10F;
	private static final float LABEL_RY = HALF_HEIGHT * 1.10F;
	private static final float ARC_STEP = 1.22F / ROWS;
	private static final float[] ARC_LENGTH = new float[ROWS + 1];
	static {
		for (int i = 1; i <= ROWS; i++) {
			double angle = (i - 0.5) * ARC_STEP;
			ARC_LENGTH[i] = ARC_LENGTH[i - 1] + ARC_STEP * (float) Math.hypot(
				LABEL_RX * Math.cos(angle), LABEL_RY * Math.sin(angle));
		}
	}
	private final Font font;
	private final SpriteGetter sprites;

	public TeleporterRenderer(BlockEntityRendererProvider.Context context) {
		sprites = context.sprites();
		font = context.font();
	}

	@Override
	public TeleporterRenderState createRenderState() {
		return new TeleporterRenderState();
	}

	@Override
	public void extractRenderState(TeleporterBlockEntity blockEntity, TeleporterRenderState state,
		float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		var block = blockEntity.getBlockState();
		state.active = block.is(ModBlocks.TELEPORTER)
			&& block.getValue(TeleporterBlock.LINKED) && block.getValue(TeleporterBlock.POWERED);
		state.label = state.active ? blockEntity.getLabel() : "";
		if (!state.active) return;
		// The block atlas advances the vanilla animation, including resource-pack overrides.
		var sprite = sprites.get(PORTAL);
		state.u0 = sprite.getU0();
		state.u1 = sprite.getU1();
		state.v0 = sprite.getV0();
		state.v1 = sprite.getV1();
		Vec3 offset = cameraPosition.subtract(Vec3.atCenterOf(blockEntity.getBlockPos()));
		state.yaw = (float) Math.atan2(offset.x, offset.z);
	}

	@Override
	public void submit(TeleporterRenderState state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
		if (!state.active) return;
		// Snapshot UVs rather than retaining mutable state in the deferred callback.
		float u0 = state.u0, u1 = state.u1, v0 = state.v0, v1 = state.v1;
		poses.pushPose();
		poses.translate(0.5, 2.12, 0.5);
		poses.mulPose(Axis.YP.rotation(state.yaw));
		collector.submitCustomGeometry(poses, PORTAL.renderType(RenderTypes::entityTranslucentEmissive),
			(pose, vertices) -> draw(pose, vertices, u0, u1, v0, v1));
		collector.submitCustomGeometry(poses, RenderTypes.debugQuads(), TeleporterRenderer::drawRim);
		if (!state.label.isEmpty()) submitLabel(state.label, poses, collector);
		poses.popPose();
	}

	private void submitLabel(String label, PoseStack poses, SubmitNodeCollector collector) {
		var text = Component.literal(label).getVisualOrderText();
		int width = font.width(text);
		if (width == 0) return;
		float scale = Math.min(0.025F, 2 * ARC_LENGTH[ROWS] / width);
		text.accept(new FormattedCharSink() {
			float distance = -width * scale / 2;

			@Override
			public boolean accept(int index, Style style, int codePoint) {
				var glyph = FormattedCharSequence.codepoint(codePoint, style);
				int advance = font.width(glyph);
				float center = distance + advance * scale / 2;
				distance += advance * scale;
				if (Character.isWhitespace(codePoint)) return true;
				float angle = angleAtDistance(center);
				float sin = (float) Math.sin(angle), cos = (float) Math.cos(angle);
				poses.pushPose();
				poses.translate(LABEL_RX * sin, LABEL_RY * cos, 0.005F);
				poses.mulPose(Axis.ZP.rotation((float) Math.atan2(-LABEL_RY * sin, LABEL_RX * cos)));
				poses.scale(scale, -scale, scale);
				collector.submitText(poses, -advance / 2F, -font.lineHeight, glyph, false,
					Font.DisplayMode.NORMAL, 15728880, 0xFF50F0FF, 0, 0xFFAE48FF);
				poses.popPose();
				return true;
			}
		});
	}

	private static float angleAtDistance(float distance) {
		float length = Math.min(Math.abs(distance), ARC_LENGTH[ROWS]);
		int index = Arrays.binarySearch(ARC_LENGTH, length);
		if (index >= 0) return Math.copySign(index * ARC_STEP, distance);
		int upper = -index - 1;
		float fraction = (length - ARC_LENGTH[upper - 1]) / (ARC_LENGTH[upper] - ARC_LENGTH[upper - 1]);
		return Math.copySign((upper - 1 + fraction) * ARC_STEP, distance);
	}

	private static void draw(PoseStack.Pose pose, VertexConsumer vertices, float u0, float u1, float v0, float v1) {
		// Tile twice vertically to retain Nether-sized pixels. Split at the repeat
		// instead of wrapping atlas UVs into neighbouring sprites.
		for (int row = 0; row < ROWS; row++) {
			float bottom = -1 + row * 2.0F / ROWS;
			float top = -1 + (row + 1) * 2.0F / ROWS;
			float bottomX = HALF_WIDTH * (float) Math.sqrt(Math.max(0, 1 - bottom * bottom));
			float topX = HALF_WIDTH * (float) Math.sqrt(Math.max(0, 1 - top * top));
			float vb = v1 - (v1 - v0) * (row % (ROWS / 2)) / (ROWS / 2);
			float vt = v1 - (v1 - v0) * (row % (ROWS / 2) + 1) / (ROWS / 2);
			vertex(pose, vertices, -bottomX, bottom * HALF_HEIGHT, u(-bottomX, u0, u1), vb);
			vertex(pose, vertices, bottomX, bottom * HALF_HEIGHT, u(bottomX, u0, u1), vb);
			vertex(pose, vertices, topX, top * HALF_HEIGHT, u(topX, u0, u1), vt);
			vertex(pose, vertices, -topX, top * HALF_HEIGHT, u(-topX, u0, u1), vt);
		}
	}

	private static float u(float x, float u0, float u1) {
		return u0 + (u1 - u0) * (0.5F + x / (2 * HALF_WIDTH));
	}

	private static void drawRim(PoseStack.Pose pose, VertexConsumer vertices) {
		for (int i = 0; i < ROWS * 2; i++) {
			rimQuad(pose, vertices, i, 1.0F, 1.025F, 174, 72, 255);
			rimQuad(pose, vertices, i, 1.025F, 1.05F, 80, 240, 255);
		}
	}

	private static void rimQuad(PoseStack.Pose pose, VertexConsumer vertices, int point,
		float inner, float outer, int red, int green, int blue) {
		int next = (point + 1) % OUTLINE_X.length;
		rimVertex(pose, vertices, point, inner, red, green, blue);
		rimVertex(pose, vertices, point, outer, red, green, blue);
		rimVertex(pose, vertices, next, outer, red, green, blue);
		rimVertex(pose, vertices, next, inner, red, green, blue);
	}

	private static void rimVertex(PoseStack.Pose pose, VertexConsumer vertices, int point,
		float radius, int red, int green, int blue) {
		vertices.addVertex(pose, OUTLINE_X[point] * HALF_WIDTH * radius,
			OUTLINE_Y[point] * HALF_HEIGHT * radius, 0).setColor(red, green, blue, 255);
	}

	private static void vertex(PoseStack.Pose pose, VertexConsumer vertices, float x, float y, float u, float v) {
		// Use the sprite's own alpha and colors without an extra glass-like tint.
		vertices.addVertex(pose, x, y, 0).setColor(255, 255, 255, 255).setUv(u, v)
			.setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0, 0, 1);
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 32;
	}
}
