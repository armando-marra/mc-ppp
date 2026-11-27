package com.project.pointportals.datagen;

import com.project.pointportals.registry.ModBlocks;
import com.project.pointportals.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

public class PointPortalsEnglishLangProvider extends FabricLanguageProvider {
	public PointPortalsEnglishLangProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	public void generateTranslations(HolderLookup.Provider registries, TranslationBuilder builder) {
		builder.add("guide.pointportals.oval", "Oval orientation");
		builder.add("guide.pointportals.oval_follow", "Follow");
		builder.add("guide.pointportals.oval_fixed", "Fixed");
		builder.add("guide.pointportals.hint", "Press %s to open the portal guide.");
		builder.add("guide.pointportals.hint_unbound", "Assign a key to the portal guide in Options → Controls.");
		builder.add("key.category.pointportals.portals", "Point-to-Point Portals");
		builder.add("key.pointportals.guide", "Open portal guide");
		builder.add("guide.pointportals.version", "Version %s");
		builder.add("guide.pointportals.release", "Released %s");
		builder.add("guide.pointportals.unreleased", "Unreleased");
		builder.add("guide.pointportals.changes", "Changes in %s");
		builder.add("guide.pointportals.appearance", "This guide and its first-join hint are new. Use Oval orientation below to follow the viewer or fix the oval east–west. Your choice is saved.");
		builder.add("guide.pointportals.cartography", "Portal maps");
		builder.add("guide.pointportals.maps", "Use a filled map on a pad to mark it. Map both ends, then sneak and use the map while looking into the air to see the pair. Copies share markers. Locked maps keep a snapshot.");
		builder.add("guide.pointportals.pair", "Pair two portals");
		builder.add("guide.pointportals.pairing", "Place two unlinked pads, even in different dimensions. Use the Phase Tuner on the first pad, then on the second. Each pad can have only one twin.");
		builder.add("guide.pointportals.travel", "Travel");
		builder.add("guide.pointportals.power", "Power the departure pad with Redstone and step onto it. Leave two blocks clear above the destination. To return, power the other pad, step off and let the cooldown expire.");
		builder.add("guide.pointportals.unlink", "Unlink a pair");
		builder.add("guide.pointportals.unlinking", "Sneak and use an empty Phase Tuner on either pad. If the tuner holds a selection, the first use clears it.");
		builder.add(ModBlocks.TELEPORTER, "Point-to-Point Teleporter");
		builder.add(ModItems.TUNER, "Phase Tuner");
		builder.add("cartography.pointportals.pair", "%s ↔ %s");
		builder.add("cartography.pointportals.no_pairs", "No mapped portal pairs.");
		builder.add("cartography.pointportals.unnamed", "Portal (%s, %s, %s)");
		builder.add("tooltip.pointportals.tuner_unbound", "Right-click an unlinked Teleporter to begin pairing.");
		builder.add("tooltip.pointportals.tuner_target", "Armed: Bound to %s at [%s, %s, %s]");
		builder.add("message.pointportals.first_portal_selected", "First portal captured at [%s, %s, %s]. Select the target portal.");
		builder.add("message.pointportals.cannot_self_link", "Cannot link a portal to itself!");
		builder.add("message.pointportals.already_linked", "This portal is already linked to another destination.");
		builder.add("message.pointportals.source_portal_missing", "The first portal could not be found or was broken!");
		builder.add("message.pointportals.invalid_dimension", "Target dimension is currently inaccessible.");
		builder.add("message.pointportals.portals_linked", "Portals successfully paired! Power with Redstone to activate.");
		builder.add("message.pointportals.portals_unlinked", "Portals successfully unlinked.");
		builder.add("message.pointportals.tuner_cleared", "Phase Tuner memory cleared.");
		builder.add("message.pointportals.label_set", "Portal label: %s");
		builder.add("message.pointportals.label_cleared", "Portal label cleared.");
		builder.add("message.pointportals.label_invalid", "Use a single line of up to %s characters.");
		builder.add("message.pointportals.status_unlinked", "Teleporter is unlinked. Use a Phase Tuner to pair.");
		builder.add("message.pointportals.status_standby", "Teleporter paired with %s at [%s, %s, %s] (Standby - requires Redstone).");
		builder.add("message.pointportals.status_active", "Teleporter paired with %s at [%s, %s, %s] (Active).");
		builder.add("message.pointportals.destination_obstructed", "Destination portal is obstructed!");
	}
}
