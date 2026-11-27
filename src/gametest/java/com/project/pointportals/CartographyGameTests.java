package com.project.pointportals;

import com.project.pointportals.block.entity.TeleporterBlockEntity;
import com.project.pointportals.cartography.PortalMapMarker;
import com.project.pointportals.registry.ModBlocks;
import java.util.List;
import java.util.stream.StreamSupport;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("removal")
public class CartographyGameTests {
	@GameTest
	public void registrationUsesLocationAndChecksTheMap(GameTestHelper test) {
		var level = test.getLevel();
		var a = test.absolutePos(new BlockPos(1, 1, 1));
		var b = a.above();
		var player = test.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		try {
			place(test, a, "Casa");
			place(test, b, "Casa");
			var map = MapItem.create(level, a.getX(), a.getZ(), (byte) 0, false, false);
			test.assertTrue(click(player, map, a), "First click must register the portal");
			test.assertTrue(click(player, map, a), "Second click must keep the marker");
			test.assertTrue(click(player, map, b), "Names and X/Z may match for two distinct portals");
			test.assertTrue(decorations(test, map).size() == 2, "Two portals with the same name need two markers");
			((TeleporterBlockEntity) level.getBlockEntity(b)).setLabel("");
			test.assertTrue(decorations(test, map).stream().anyMatch(marker -> marker.name().isEmpty()),
				"An unnamed portal must keep its icon without a caption");

			var far = MapItem.create(level, a.getX() + 1000, a.getZ(), (byte) 0, false, false);
			var nether = level.getServer().getLevel(Level.NETHER);
			var foreign = MapItem.create(nether, a.getX(), a.getZ(), (byte) 0, false, false);
			test.assertFalse(click(player, far, a), "Out-of-area map must be rejected");
			test.assertFalse(click(player, foreign, a), "Map of a different dimension must be rejected");
			test.assertTrue(decorations(test, far).isEmpty() && decorations(test, foreign).isEmpty(),
				"Rejected maps must not be modified");
			test.succeed();
		} finally {
			removePlayer(player);
		}
	}

	@GameTest
	public void cartographyOperationsKeepLiveAndFrozenMarkers(GameTestHelper test) {
		var level = test.getLevel();
		var pos = test.absolutePos(new BlockPos(1, 1, 1));
		var player = test.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		try {
			place(test, pos, "Casa");
			var map = MapItem.create(level, pos.getX(), pos.getZ(), (byte) 0, false, false);
			test.assertTrue(click(player, map, pos), "Click must register the portal");
			var copy = cartography(player, pos, map, Items.MAP);
			var scaled = cartography(player, pos, map, Items.PAPER);
			var frozen = cartography(player, pos, map, Items.GLASS_PANE);
			test.assertTrue(copy.getCount() == 2 && copy.get(DataComponents.MAP_ID).equals(map.get(DataComponents.MAP_ID)),
				"Cartography copies must share the map ID");
			test.assertTrue(MapItem.getSavedData(scaled, level).scale == 1 && decorations(test, scaled).size() == 1,
				"Scaling must retain a registered portal inside the new bounds");
			test.assertTrue(MapItem.getSavedData(frozen, level).locked, "Glass pane must lock the map");
			test.assertFalse(click(player, frozen, pos), "Locked maps must reject edits");
			((TeleporterBlockEntity) level.getBlockEntity(pos)).setLabel("Base");
			for (var live : List.of(map, copy, scaled)) {
				test.assertTrue(hasName(test, live, "Base"), "Live copies and scaled maps must reflect renames");
			}
			test.assertTrue(hasName(test, frozen, "Casa"), "Frozen map must retain the original name");
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			place(test, pos, "Nuova");
			test.assertTrue(click(player, scaled, pos), "Replacement can be explicitly registered");
			test.assertTrue(decorations(test, map).isEmpty(), "Replacement must not revive the old marker");
			test.assertTrue(hasName(test, frozen, "Casa"), "Frozen marker must survive destruction");
			test.succeed();
		} finally {
			removePlayer(player);
		}
	}

	private static ItemStack cartography(ServerPlayer player, BlockPos pos, ItemStack map, Item material) {
		var table = new CartographyTableMenu(1, player.getInventory(), ContainerLevelAccess.create(player.level(), pos));
		table.container.setItem(CartographyTableMenu.MAP_SLOT, map.copy());
		table.container.setItem(CartographyTableMenu.ADDITIONAL_SLOT, new ItemStack(material));
		var result = table.getSlot(CartographyTableMenu.RESULT_SLOT).getItem().copy();
		result.getItem().onCraftedBy(result, player);
		return result;
	}

	private static boolean hasName(GameTestHelper test, ItemStack map, String name) {
		var markers = decorations(test, map);
		return markers.size() == 1 && markers.getFirst().name().orElseThrow().getString().equals(name);
	}

	private static void place(GameTestHelper test, BlockPos pos, String name) {
		var level = test.getLevel();
		level.setBlock(pos, ModBlocks.TELEPORTER.defaultBlockState(), Block.UPDATE_ALL);
		((TeleporterBlockEntity) level.getBlockEntity(pos)).setLabel(name);
	}

	private static boolean click(ServerPlayer player, ItemStack map, BlockPos pos) {
		player.setItemInHand(InteractionHand.MAIN_HAND, map);
		return player.gameMode.useItemOn(player, player.level(), map, InteractionHand.MAIN_HAND,
			new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)).consumesAction();
	}

	private static List<MapDecoration> decorations(GameTestHelper test, ItemStack map) {
		var data = MapItem.getSavedData(map, test.getLevel());
		return StreamSupport.stream(data.getDecorations().spliterator(), false)
			.filter(marker -> marker.type().is(PortalMapMarker.TYPE)).toList();
	}

	private static void removePlayer(ServerPlayer player) {
		player.level().getServer().getPlayerList().remove(player);
		player.discard();
	}
}
