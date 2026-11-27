package com.project.pointportals.client;

import com.project.pointportals.block.TeleporterBlock;
import com.project.pointportals.block.entity.TeleporterBlockEntity;
import com.project.pointportals.client.render.TeleporterRenderer;
import com.project.pointportals.client.render.TeleporterRenderState;
import com.project.pointportals.component.PortalTarget;
import com.project.pointportals.registry.ModBlocks;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.lwjgl.glfw.GLFW;

/** Captures actual rendered frames before/after loss of power in an isolated world. */
@SuppressWarnings("UnstableApiUsage")
public final class PortalVisualTest implements FabricClientGameTest {
	private static final BlockPos A = new BlockPos(0, 64, 0);
	private static final BlockPos B = new BlockPos(8, 64, 0);

	@Override
	public void runTest(ClientGameTestContext context) {
		try (var world = context.worldBuilder().create()) {
			var server = world.getServer();
			server.runCommand("fill -8 63 -8 12 63 12 minecraft:stone");
			server.runCommand("time set noon");
			server.runOnServer(instance -> {
				var level = instance.overworld();
				level.setBlock(A.below(), Blocks.REDSTONE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
				level.setBlock(A, ModBlocks.TELEPORTER.defaultBlockState(), Block.UPDATE_ALL);
				level.setBlock(B, ModBlocks.TELEPORTER.defaultBlockState(), Block.UPDATE_ALL);
				((TeleporterBlockEntity) level.getBlockEntity(A)).setLabel("Miniere del Nord");
				((TeleporterBlockEntity) level.getBlockEntity(A)).setTwinTarget(PortalTarget.of(level, B));
				((TeleporterBlockEntity) level.getBlockEntity(B)).setTwinTarget(PortalTarget.of(level, A));
				level.setBlock(A, level.getBlockState(A).setValue(TeleporterBlock.LINKED, true), Block.UPDATE_ALL);
				level.setBlock(B, level.getBlockState(B).setValue(TeleporterBlock.LINKED, true), Block.UPDATE_ALL);
			});
			server.runCommand("gamemode spectator @a");
			server.runCommand("tp @a 2.5 65 3.5 146.31 8");
			context.waitFor(client -> client.player != null
				&& client.player.position().distanceToSqr(new Vec3(2.5, 65, 3.5)) < 0.01);
			context.waitFor(client -> client.level != null && client.level.getBlockState(A).is(ModBlocks.TELEPORTER)
				&& client.level.getBlockState(A).getValue(TeleporterBlock.POWERED)
				&& client.level.getBlockState(A).getValue(TeleporterBlock.LINKED));
			world.getConnection().waitForChunksRender();
			world.getConnection().waitForClientboundPackets();
			context.getInput().pressKey(GLFW.GLFW_KEY_K);
			context.waitForScreen(BookViewScreen.class);
			context.getInput().pressKey(GLFW.GLFW_KEY_PAGE_DOWN);
			context.waitFor(client -> client.gui.screen() instanceof BookViewScreen guide
				&& guide.getNarrationMessage().getString().contains("0.2.2")
				&& guide.getNarrationMessage().getString().contains("east–west"));
			context.runOnClient(client -> {
				var guide = client.gui.screen();
				var orientation = guide.children().stream().filter(CycleButton.class::isInstance).findFirst().orElseThrow();
				guide.setFocused(orientation);
			});
			context.getInput().pressKey(GLFW.GLFW_KEY_ENTER);
			context.waitFor(client -> !PortalAppearance.facesViewer());
			try {
				assertOrientation(context, true);
			} finally {
				context.getInput().pressKey(GLFW.GLFW_KEY_ENTER);
				context.waitFor(client -> PortalAppearance.facesViewer());
			}
			assertOrientation(context, false);
			context.getInput().pressKey(GLFW.GLFW_KEY_ESCAPE);
			context.waitFor(client -> client.gui.screen() == null);
			context.getInput().resizeWindow(1280, 720);
			context.getInput().lookAt(146.31F, 8);
			context.getInput().pressKey(GLFW.GLFW_KEY_F1);
			context.waitTicks(5);
			assertState(context, true);
			Path active = context.takeScreenshot("oval-active");
			server.runOnServer(instance -> ((TeleporterBlockEntity) instance.overworld().getBlockEntity(A)).setLabel(""));
			context.waitFor(client -> ((TeleporterBlockEntity) client.level.getBlockEntity(A)).getLabel().isEmpty());
			context.waitTicks(2);
			Path bare = context.takeScreenshot("oval-without-label");
			verifyLabelAboveRim(active, bare);
			server.runOnServer(instance -> ((TeleporterBlockEntity) instance.overworld().getBlockEntity(A)).setLabel("Miniere del Nord"));
			context.waitFor(client -> ((TeleporterBlockEntity) client.level.getBlockEntity(A)).getLabel().equals("Miniere del Nord"));
			context.waitTicks(15);
			Path animated = context.takeScreenshot("oval-active-animated");
			server.runOnServer(instance -> instance.overworld().setBlock(A.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL));
			context.waitFor(client -> !client.level.getBlockState(A).getValue(TeleporterBlock.POWERED));
			assertState(context, false);
			context.waitTicks(2);
			Path standby = context.takeScreenshot("oval-standby");
			server.runOnServer(instance -> {
				var level = instance.overworld();
				level.setBlock(B, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
				level.setBlock(A.below(), Blocks.REDSTONE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
			});
			context.waitFor(client -> !client.level.getBlockState(A).getValue(TeleporterBlock.LINKED)
				&& client.level.getBlockState(A).getValue(TeleporterBlock.POWERED));
			assertState(context, false);
			context.waitTicks(2);
			Path unlinked = context.takeScreenshot("oval-unlinked-powered");
			verifyFrames(active, animated, standby, unlinked);
		}
	}

	private static void verifyLabelAboveRim(Path labeled, Path bare) {
		try {
			var before = ImageIO.read(bare.toFile());
			var after = ImageIO.read(labeled.toFile());
			int top = before.getHeight();
			for (int x = before.getWidth() / 3; x < before.getWidth() * 2 / 3; x++) {
				for (int y = 0; y < top; y++) {
					if (isCyan(before.getRGB(x, y))) top = y;
				}
			}
			int letters = 0;
			for (int y = 0; y < top; y++) {
				for (int x = after.getWidth() / 3; x < after.getWidth() * 2 / 3; x++) {
					if (isCyan(after.getRGB(x, y))) letters++;
				}
			}
			if (letters < 50) throw new AssertionError("Label must add visible cyan lettering above the upper rim");
		} catch (IOException e) {
			throw new AssertionError("Unable to inspect label screenshots", e);
		}
	}

	private static boolean isCyan(int color) {
		return (color >> 16 & 255) < 140 && (color >> 8 & 255) > 170 && (color & 255) > 200;
	}

	private static void verifyFrames(Path active, Path animated, Path standby, Path unlinked) {
		try {
			var first = ImageIO.read(active.toFile());
			var next = ImageIO.read(animated.toFile());
			int purple = portalPixels(first);
			if (purple < portalPixels(ImageIO.read(standby.toFile())) + 1000
				|| purple < portalPixels(ImageIO.read(unlinked.toFile())) + 1000) {
				throw new AssertionError("The Nether-textured oval must be visible and disappear without power or a link");
			}
			if (rimPixels(first) < rimPixels(ImageIO.read(standby.toFile())) + 100) {
				throw new AssertionError("The active oval must retain a visible cyan outline");
			}
			int changed = 0;
			for (int y = first.getHeight() / 8; y < first.getHeight() * 4 / 5; y++) {
				for (int x = first.getWidth() / 3; x < first.getWidth() * 2 / 3; x++) {
					if (first.getRGB(x, y) != next.getRGB(x, y)) changed++;
				}
			}
			if (changed < 200) throw new AssertionError("The energy surface must animate between frames");
		} catch (IOException e) {
			throw new AssertionError("Unable to inspect rendered screenshots", e);
		}
	}

	private static int portalPixels(BufferedImage image) {
		int count = 0;
		// Above the pad: exclude its own purple/cyan texture and count the oval surface.
		for (int y = image.getHeight() / 8; y < image.getHeight() * 5 / 8; y++) {
			for (int x = image.getWidth() / 3; x < image.getWidth() * 2 / 3; x++) {
				int color = image.getRGB(x, y);
				int red = color >> 16 & 255, green = color >> 8 & 255, blue = color & 255;
				if (red > green * 1.25 && blue > green * 1.5 && blue > 80) count++;
			}
		}
		return count;
	}

	private static int rimPixels(BufferedImage image) {
		int count = 0;
		for (int y = image.getHeight() / 8; y < image.getHeight() * 5 / 8; y++) {
			for (int x = image.getWidth() / 3; x < image.getWidth() * 2 / 3; x++) {
				int color = image.getRGB(x, y);
				if (isCyan(color)) count++;
			}
		}
		return count;
	}

	private static void assertOrientation(ClientGameTestContext context, boolean fixed) {
		context.runOnClient(client -> {
			var portal = (TeleporterBlockEntity) client.level.getBlockEntity(A);
			var renderer = client.getBlockEntityRenderDispatcher().<TeleporterBlockEntity, TeleporterRenderState>getRenderer(portal);
			var state = renderer.createRenderState();
			var center = Vec3.atCenterOf(A);
			renderer.extractRenderState(portal, state, 0, center.add(5, 0, 0), null);
			float fromEast = state.yaw;
			renderer.extractRenderState(portal, state, 0, center.add(0, 0, -5), null);
			float fromNorth = state.yaw;
			if (fixed ? fromEast != 0 || fromNorth != 0 || !state.labelOnBack
				: Math.abs(fromEast - Math.PI / 2) > 0.001 || Math.abs(fromNorth - Math.PI) > 0.001) {
				throw new AssertionError("Unexpected oval orientation at different camera positions");
			}
		});
	}

	private static void assertState(ClientGameTestContext context, boolean active) {
		context.runOnClient(client -> {
			var blockEntity = (TeleporterBlockEntity) client.level.getBlockEntity(A);
			int expectedLight = blockEntity.getBlockState().getValue(TeleporterBlock.LINKED) ? 12 : 0;
			if (blockEntity.getBlockState().getLightEmission() != expectedLight) {
				throw new AssertionError("Linked standby and active pads must keep the previous active light level");
			}
			var renderer = client.getBlockEntityRenderDispatcher().<TeleporterBlockEntity, TeleporterRenderState>getRenderer(blockEntity);
			if (!(renderer instanceof TeleporterRenderer)) throw new AssertionError("Teleporter renderer must be registered");
			var state = renderer.createRenderState();
			renderer.extractRenderState(blockEntity, state, 0, client.player.position(), null);
			if (!blockEntity.getLabel().equals("Miniere del Nord") || !state.label.equals(active ? "Miniere del Nord" : "")) {
				throw new AssertionError("The label must stay synchronized and only render on the active oval");
			}
			if (state.active != active) throw new AssertionError("Unexpected portal render activation");
		});
	}
}
