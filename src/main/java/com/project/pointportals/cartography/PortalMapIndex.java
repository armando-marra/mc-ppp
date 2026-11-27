package com.project.pointportals.cartography;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.project.pointportals.PointPortals;
import com.project.pointportals.block.entity.TeleporterBlockEntity;
import com.project.pointportals.component.PortalTarget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jspecify.annotations.Nullable;

/** Persistent records for portals explicitly added to at least one map. */
public final class PortalMapIndex {
	private static final Codec<Map<PortalTarget, Entry>> CODEC = Entry.CODEC.listOf().xmap(entries -> {
		Map<PortalTarget, Entry> result = new HashMap<>();
		entries.forEach(entry -> result.put(entry.target(), entry));
		return Map.copyOf(result);
	}, entries -> List.copyOf(entries.values()));
	private static final AttachmentType<Map<PortalTarget, Entry>> RECORDS =
		AttachmentRegistry.createPersistent(PointPortals.id("mapped_portals"), CODEC);

	private PortalMapIndex() {}

	public static void initialize() {}

	public static PortalMapMarker register(ServerLevel level, TeleporterBlockEntity portal) {
		PortalTarget target = PortalTarget.of(level, portal.getBlockPos());
		Entry previous = entries(level).get(target);
		Entry entry = entryFor(portal, target, previous == null ? UUID.randomUUID() : previous.id());
		replace(level, target, entry);
		return new PortalMapMarker(target.pos(), entry.id(), entry.label());
	}

	public static void update(ServerLevel level, TeleporterBlockEntity portal) {
		PortalTarget target = PortalTarget.of(level, portal.getBlockPos());
		Entry entry = entries(level).get(target);
		if (entry != null) replace(level, target, entryFor(portal, target, entry.id()));
	}

	private static Entry entryFor(TeleporterBlockEntity portal, PortalTarget target, UUID id) {
		return new Entry(target, id, portal.getLabel(), Optional.ofNullable(portal.getTwinTarget()));
	}

	public static void remove(ServerLevel level, PortalTarget target) {
		replace(level, target, null);
	}

	public static void refresh(ServerLevel level, MapItemSavedData map) {
		if (map.locked) return;
		var storage = PortalMapMarker.storage(map);
		var markers = storage.pointportals$markers();
		if (markers.isEmpty()) return;
		var entries = entries(level);
		for (var marker : markers) {
			var entry = entries.get(new PortalTarget(map.dimension, marker.pos()));
			if (entry == null || !entry.id().equals(marker.portalId())) {
				storage.pointportals$removeMarker(marker.pos());
			} else if (!entry.label().equals(marker.label())) {
				storage.pointportals$putMarker(new PortalMapMarker(marker.pos(), marker.portalId(), entry.label()));
			}
		}
	}

	public static List<Component> pairs(ServerLevel level, MapItemSavedData map) {
		var entries = entries(level);
		var displayed = new HashSet<PortalTarget>();
		var pages = new ArrayList<Component>();
		for (var marker : PortalMapMarker.storage(map).pointportals$markers()) {
			var local = new PortalTarget(map.dimension, marker.pos());
			var entry = entries.get(local);
			if (entry == null || !entry.id().equals(marker.portalId()) || entry.twin().isEmpty()
				|| displayed.contains(local)) continue;

			var remote = entry.twin().get();
			var twin = entries.get(remote);
			if (remote.equals(local) || twin == null || twin.twin().filter(local::equals).isEmpty()
				|| level.getServer().getLevel(remote.dimension()) == null) continue;

			var page = Component.translatable("cartography.pointportals.pair", name(local, marker.label()),
				name(remote, twin.label())).append("\n\n");
			if (local.dimension().equals(remote.dimension())) {
				page.append(local.dimension().identifier().toString());
			} else {
				page.append(Component.translatable("cartography.pointportals.pair",
					local.dimension().identifier().toString(), remote.dimension().identifier().toString()));
			}
			pages.add(page);
			displayed.add(remote);
		}
		return List.copyOf(pages);
	}

	private static Component name(PortalTarget target, String label) {
		if (!label.isEmpty()) return Component.literal(label);
		BlockPos pos = target.pos();
		return Component.translatable("cartography.pointportals.unnamed", pos.getX(), pos.getY(), pos.getZ());
	}

	private static Map<PortalTarget, Entry> entries(ServerLevel level) {
		return level.getServer().globalAttachments().getAttachedOrElse(RECORDS, Map.of());
	}

	private static void replace(ServerLevel level, PortalTarget target, @Nullable Entry replacement) {
		var entries = entries(level);
		if (Objects.equals(entries.get(target), replacement)) return;
		var updated = new HashMap<>(entries);
		if (replacement == null) {
			updated.remove(target);
		} else {
			updated.put(target, replacement);
		}
		level.getServer().globalAttachments().setAttached(RECORDS, Map.copyOf(updated));
	}

	private record Entry(PortalTarget target, UUID id, String label, Optional<PortalTarget> twin) {
		private static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PortalTarget.CODEC.fieldOf("target").forGetter(Entry::target),
			UUIDUtil.CODEC.fieldOf("id").forGetter(Entry::id),
			Codec.STRING.optionalFieldOf("label", "").forGetter(Entry::label),
			PortalTarget.CODEC.optionalFieldOf("twin").forGetter(Entry::twin)
		).apply(instance, Entry::new));
	}
}
