package com.project.pointportals.registry;

import com.project.pointportals.PointPortals;
import com.project.pointportals.component.PortalTarget;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModDataComponents {
	public static final DataComponentType<PortalTarget> PORTAL_TARGET = Registry.register(
		BuiltInRegistries.DATA_COMPONENT_TYPE,
		PointPortals.id("portal_target"),
		DataComponentType.<PortalTarget>builder()
			.persistent(PortalTarget.CODEC)
			.networkSynchronized(PortalTarget.STREAM_CODEC)
			.build()
	);

	public static void initialize() {
		// Triggers static initialization
	}

	private ModDataComponents() {}
}
