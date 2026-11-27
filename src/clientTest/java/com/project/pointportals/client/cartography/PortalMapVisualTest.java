package com.project.pointportals.client.cartography;

import com.project.pointportals.block.TeleporterBlock;
import com.project.pointportals.block.entity.TeleporterBlockEntity;
import com.project.pointportals.cartography.PortalMapMarker;
import com.project.pointportals.component.PortalTarget;
import com.project.pointportals.registry.ModBlocks;
import java.util.stream.StreamSupport;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

@SuppressWarnings("UnstableApiUsage")
public final class PortalMapVisualTest implements FabricClientGameTest {
	private static final BlockPos PORTAL = new BlockPos(0, 64, 0);
	private static final BlockPos TWIN = new BlockPos(4, 64, 0);
	private static final BlockPos FRAME = new BlockPos(2, 66, 0);
	private static final String LABEL = "Casa";

	@Override
	public void runTest(ClientGameTestContext context) {
		TestWorldSave save;
		MapId locked;
		try (var world = context.worldBuilder().create()) {
			save = world.getWorldSave();
			var server = world.getServer();
			server.runCommand("fill -5 64 -5 5 64 8 minecraft:stone");
			server.runCommand("setblock 2 66 -1 minecraft:stone");
			server.runCommand("tp @a 2.5 65 3.5 180 0");
			server.runCommand("time set noon");
			var maps = server.computeOnServer(instance -> {
				var level = instance.overworld();
				var player = instance.getPlayerList().getPlayers().getFirst();
				level.setBlock(PORTAL, ModBlocks.TELEPORTER.defaultBlockState(), Block.UPDATE_ALL);
				var portal = (TeleporterBlockEntity) level.getBlockEntity(PORTAL);
				portal.setLabel(LABEL);
				level.setBlock(TWIN, ModBlocks.TELEPORTER.defaultBlockState(), Block.UPDATE_ALL);
				var twin = (TeleporterBlockEntity) level.getBlockEntity(TWIN);
				twin.setLabel("Miniera");
				portal.setTwinTarget(PortalTarget.of(level, TWIN));
				twin.setTwinTarget(PortalTarget.of(level, PORTAL));
				level.setBlock(PORTAL, portal.getBlockState().setValue(TeleporterBlock.LINKED, true), Block.UPDATE_ALL);
				level.setBlock(TWIN, twin.getBlockState().setValue(TeleporterBlock.LINKED, true), Block.UPDATE_ALL);

				ItemStack map = MapItem.create(level, PORTAL.getX(), PORTAL.getZ(), (byte) 0, false, false);
				MapId mapId = map.get(DataComponents.MAP_ID);
				var hit = new BlockHitResult(Vec3.atCenterOf(PORTAL), Direction.UP, PORTAL, false);
				if (!portal.getBlockState().useItemOn(map, level, player, InteractionHand.MAIN_HAND, hit).consumesAction()) {
					throw new AssertionError("Using the map on the portal must register its marker");
				}
				var twinMap = MapItem.create(level, TWIN.getX(), TWIN.getZ(), (byte) 0, false, false);
				var twinHit = new BlockHitResult(Vec3.atCenterOf(TWIN), Direction.UP, TWIN, false);
				if (!twin.getBlockState().useItemOn(twinMap, level, player, InteractionHand.MAIN_HAND, twinHit).consumesAction()) {
					throw new AssertionError("Using a separate map must register the twin");
				}
				var table = new CartographyTableMenu(1, player.getInventory(), ContainerLevelAccess.create(level, FRAME));
				table.container.setItem(CartographyTableMenu.MAP_SLOT, map);
				table.container.setItem(CartographyTableMenu.ADDITIONAL_SLOT, new ItemStack(Items.MAP));
				var output = table.getSlot(CartographyTableMenu.RESULT_SLOT).getItem();
				if (output.getCount() != 2 || !mapId.equals(output.get(DataComponents.MAP_ID))) {
					throw new AssertionError("Cartography table must produce two maps sharing the original ID");
				}
				var frame = new ItemFrame(level, FRAME, Direction.SOUTH);
				frame.setItem(output.copyWithCount(1));
				level.addFreshEntity(frame);
				player.getInventory().setItem(player.getInventory().getSelectedSlot(), output.copyWithCount(1));
				table.container.setItem(CartographyTableMenu.ADDITIONAL_SLOT, new ItemStack(Items.GLASS_PANE));
				var frozen = table.getSlot(CartographyTableMenu.RESULT_SLOT).getItem().copy();
				frozen.getItem().onCraftedBy(frozen, player);
				return new Maps(mapId, frozen.get(DataComponents.MAP_ID));
			});
			locked = maps.locked();
			world.getConnection().waitForChunksRender();
			checkClient(context, maps.live(), LABEL);
			context.getInput().resizeWindow(1280, 720);
			boolean hideGui = context.computeOnClient(client -> client.gui.hud.isHidden());
			try {
				if (hideGui) context.runOnClient(client -> client.gui.hud.toggle());
				context.getInput().lookAt(180, 32);
				context.waitTicks(20);
				context.takeScreenshot("portal-map-held");
			} finally {
				if (hideGui) context.runOnClient(client -> client.gui.hud.toggle());
			}
			context.getInput().lookAt(180, -45);
			context.getInput().holdShift();
			try {
				context.waitTicks(4);
				context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
				context.waitForScreen(BookViewScreen.class);
				context.takeScreenshot("portal-map-pair-details");
			} finally {
				context.getInput().releaseShift();
			}
			context.getInput().pressKey(GLFW.GLFW_KEY_ESCAPE);
			server.runOnServer(instance ->
				((TeleporterBlockEntity) instance.overworld().getBlockEntity(PORTAL)).setLabel("Base"));
			checkClient(context, maps.live(), "Base");
		}
		try (var world = save.open()) {
			var id = world.getServer().computeOnServer(instance -> {
				var level = instance.overworld();
				var player = instance.getPlayerList().getPlayers().getFirst();
				var map = player.getMainHandItem();
				if (!hasMarker(MapItem.getSavedData(map, level), "Base") || !hasMarker(level.getMapData(locked), LABEL)) {
					throw new AssertionError("Live and locked markers must keep their respective names after reopening");
				}
				return map.get(DataComponents.MAP_ID);
			});
			world.getConnection().waitForChunksRender();
			checkClient(context, id, "Base");
			world.getServer().runCommand("item replace entity @a weapon.mainhand with minecraft:air");
			world.getServer().runCommand("tp @a 2.5 65 1.5 180 8");
			context.waitFor(client -> client.player.getMainHandItem().isEmpty());
			context.waitTicks(10);
			context.takeScreenshot("portal-map-framed-reopened");
			world.getServer().runOnServer(instance ->
				((TeleporterBlockEntity) instance.overworld().getBlockEntity(PORTAL)).setLabel("Stazione"));
			context.waitFor(client -> hasMarker(client.level.getMapData(id), "Stazione"));
			world.getServer().runOnServer(instance -> instance.overworld().setBlock(PORTAL, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL));
			context.waitFor(client -> {
				var map = client.level.getMapData(id);
				return map != null && StreamSupport.stream(map.getDecorations().spliterator(), false)
					.noneMatch(decoration -> decoration.type().is(PortalMapMarker.TYPE));
			});
		}
	}

	private static void checkClient(ClientGameTestContext context, MapId id, String label) {
		context.waitFor(client -> client.player != null && client.level != null
			&& id.equals(client.player.getMainHandItem().get(DataComponents.MAP_ID))
			&& hasMarker(client.level.getMapData(id), label)
			&& client.level.getEntitiesOfClass(ItemFrame.class, new AABB(FRAME).inflate(1))
				.stream().anyMatch(frame -> id.equals(frame.getItem().get(DataComponents.MAP_ID))));
	}

	private static boolean hasMarker(MapItemSavedData data, String label) {
		if (data == null) return false;
		for (var decoration : data.getDecorations()) {
			if (decoration.type().is(PortalMapMarker.TYPE) && decoration.renderOnFrame()
				&& decoration.name().filter(name -> label.equals(name.getString())).isPresent()) return true;
		}
		return false;
	}

	private record Maps(MapId live, MapId locked) {}
}
