package com.project.pointportals.client;

import com.project.pointportals.PointPortals;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class PortalAppearance {
	private static final Path FIXED_OVAL = FabricLoader.getInstance().getConfigDir().resolve("pointportals-fixed-oval");
	private static boolean facesViewer = !Files.exists(FIXED_OVAL);

	private PortalAppearance() {}

	public static boolean facesViewer() {
		return facesViewer;
	}

	static void setFacesViewer(boolean enabled) {
		if (enabled == facesViewer) return;
		try {
			if (enabled) {
				Files.deleteIfExists(FIXED_OVAL);
			} else {
				Files.createDirectories(FIXED_OVAL.getParent());
				Files.createFile(FIXED_OVAL);
			}
		} catch (IOException error) {
			PointPortals.LOGGER.warn("Could not save the portal oval orientation", error);
		}
		facesViewer = enabled;
	}
}
