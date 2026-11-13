package com.project.pointportals.diagnostics;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Measures only explicit synchronous chunk resolution, without retaining chunks or tickets. */
public final class PortalChunkLoads {
	private PortalChunkLoads() {
	}

	public static void resolve(ServerLevel level, BlockPos pos, String operation) {
		ChunkLoadEvent event = new ChunkLoadEvent();
		if (!event.isEnabled()) {
			level.getChunk(pos);
			return;
		}
		event.operation = operation;
		event.dimension = level.dimension().identifier().toString();
		event.chunkX = pos.getX() >> 4;
		event.chunkZ = pos.getZ() >> 4;
		event.loadedBefore = level.isLoaded(pos);
		event.begin();
		try {
			level.getChunk(pos);
			event.completed = true;
		} finally {
			event.end();
			event.commit();
		}
	}

	@Name("pointportals.ChunkLoad")
	@Label("Portal synchronous chunk resolution")
	@Category({"Point-to-Point Portals", "Performance"})
	@StackTrace(false)
	static final class ChunkLoadEvent extends Event {
		@Label("Operation")
		String operation;
		@Label("Destination dimension")
		String dimension;
		int chunkX;
		int chunkZ;
		@Label("Chunk already loaded")
		boolean loadedBefore;
		@Label("Resolution completed")
		boolean completed;
	}
}
