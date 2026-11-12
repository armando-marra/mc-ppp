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
		builder.add(ModBlocks.TELEPORTER, "Point-to-Point Teleporter");
		builder.add(ModItems.TUNER, "Phase Tuner");
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
		builder.add("message.pointportals.status_unlinked", "Teleporter is unlinked. Use a Phase Tuner to pair.");
		builder.add("message.pointportals.status_standby", "Teleporter paired with %s at [%s, %s, %s] (Standby - requires Redstone).");
		builder.add("message.pointportals.status_active", "Teleporter paired with %s at [%s, %s, %s] (Active).");
		builder.add("message.pointportals.destination_obstructed", "Destination portal is obstructed!");
	}
}
