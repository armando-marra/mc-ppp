package com.project.pointportals.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.project.pointportals.PointPortals;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.CommonComponents;
import org.lwjgl.glfw.GLFW;

final class PortalGuide {
	private static final SystemToast.SystemToastId GUIDE_HINT = new SystemToast.SystemToastId(8000L);
	private static boolean firstJoinHandled;

	private PortalGuide() {}

	static void initialize() {
		var category = KeyMapping.Category.register(PointPortals.id("portals"));
		var open = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.pointportals.guide", GLFW.GLFW_KEY_K, category));
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			if (!open.isDefault()) return;
			// Options now include saved bindings and keys registered by other mods.
			for (int code : new int[]{GLFW.GLFW_KEY_K, GLFW.GLFW_KEY_KP_MULTIPLY, GLFW.GLFW_KEY_KP_DIVIDE}) {
				var candidate = InputConstants.Type.KEYSYM.getOrCreate(code);
				if (Arrays.stream(client.options.keyMappings).noneMatch(other -> other != open
						&& (other.matches(candidate) || other.getDefaultKey().equals(candidate)))) {
					open.setKey(candidate);
					KeyMapping.resetMapping();
					return;
				}
			}
			open.setKey(InputConstants.UNKNOWN);
			KeyMapping.resetMapping();
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!firstJoinHandled && client.player != null && client.gui.screen() == null && !client.gui.hud.isHidden()) {
				firstJoinHandled = true;
				if (markHintSeen()) {
					var message = open.isUnbound()
						? Component.translatable("guide.pointportals.hint_unbound")
						: Component.translatable("guide.pointportals.hint", open.getTranslatedKeyMessage());
					SystemToast.add(client.gui.toastManager(), GUIDE_HINT, Component.literal("Point-to-Point Portals"), message);
				}
			}
			while (open.consumeClick()) {
				if (client.player != null && client.gui.screen() == null) {
					client.gui.setScreen(createScreen());
				}
			}
		});
	}

	private static boolean markHintSeen() {
		var config = FabricLoader.getInstance().getConfigDir();
		try {
			Files.createDirectories(config);
			Files.createFile(config.resolve("pointportals-guide-seen"));
			return true;
		} catch (FileAlreadyExistsException existing) {
			return false;
		} catch (IOException error) {
			PointPortals.LOGGER.warn("Could not save the portal guide notification state", error);
			return true;
		}
	}

	private static BookViewScreen createScreen() {
		var metadata = FabricLoader.getInstance().getModContainer(PointPortals.MOD_ID).orElseThrow().getMetadata();
		var date = metadata.getCustomValue("pointportals:release_date").getAsString();
		var version = metadata.getVersion().getFriendlyString();
		var cover = Component.literal(metadata.getName()).withStyle(ChatFormatting.BOLD).append("\n\n")
			.append(Component.translatable("guide.pointportals.version", version))
			.append("\n").append(date.isEmpty() ? Component.translatable("guide.pointportals.unreleased")
				: Component.translatable("guide.pointportals.release", date));
		return new BookViewScreen(new BookViewScreen.BookAccess(List.of(cover,
			page("changes", "appearance", version), page("pair", "pairing"), page("travel", "power"),
			page("unlink", "unlinking"), page("cartography", "maps")))) {
			@Override
			protected void createMenuControls() {
				int left = (width - 216) / 2;
				int top = menuControlsTop();
				addRenderableWidget(CycleButton.booleanBuilder(
					Component.translatable("guide.pointportals.oval_follow"),
					Component.translatable("guide.pointportals.oval_fixed"),
					PortalAppearance.facesViewer())
					.create(left, top, 150, 20, Component.translatable("guide.pointportals.oval"),
						(button, follow) -> PortalAppearance.setFacesViewer(follow)));
				addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
					.bounds(left + 156, top, 60, 20).build());
			}
		};
	}

	private static Component page(String title, String body, Object... titleArgs) {
		return Component.empty().append(Component.translatable("guide.pointportals." + title, titleArgs).withStyle(ChatFormatting.BOLD))
			.append("\n\n").append(Component.translatable("guide.pointportals." + body));
	}
}
