package com.project.pointportals.datagen;

import com.project.pointportals.registry.ModBlocks;
import com.project.pointportals.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

public class PointPortalsGermanLangProvider extends FabricLanguageProvider {
	public PointPortalsGermanLangProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, "de_de", registries);
	}

	@Override
	public void generateTranslations(HolderLookup.Provider registries, TranslationBuilder builder) {
		builder.add(ModBlocks.TELEPORTER, "Punkt-zu-Punkt-Teleporter");
		builder.add(ModItems.TUNER, "Phasenabstimmer");
		builder.add("tooltip.pointportals.tuner_unbound", "Klicke mit der rechten Maustaste auf einen unverknüpften Teleporter, um die Kopplung zu beginnen.");
		builder.add("tooltip.pointportals.tuner_target", "Auswahl: %s bei [%s, %s, %s]");
		builder.add("message.pointportals.first_portal_selected", "Erstes Portal bei [%s, %s, %s] ausgewählt. Wähle das Zielportal aus.");
		builder.add("message.pointportals.cannot_self_link", "Ein Portal kann nicht mit sich selbst verknüpft werden!");
		builder.add("message.pointportals.already_linked", "Dieses Portal ist bereits mit einem anderen Ziel verknüpft.");
		builder.add("message.pointportals.source_portal_missing", "Das erste Portal wurde nicht gefunden oder zerstört!");
		builder.add("message.pointportals.invalid_dimension", "Die Zieldimension ist derzeit nicht zugänglich.");
		builder.add("message.pointportals.portals_linked", "Portale gekoppelt! Versorge sie mit Redstone, um sie zu aktivieren.");
		builder.add("message.pointportals.portals_unlinked", "Verknüpfung der Portale aufgehoben.");
		builder.add("message.pointportals.tuner_cleared", "Speicher des Phasenabstimmers gelöscht.");
		builder.add("message.pointportals.status_unlinked", "Der Teleporter ist nicht verknüpft. Verwende einen Phasenabstimmer zum Koppeln.");
		builder.add("message.pointportals.status_standby", "Teleporter mit %s bei [%s, %s, %s] verknüpft (Bereit: benötigt Redstone).");
		builder.add("message.pointportals.status_active", "Teleporter mit %s bei [%s, %s, %s] verknüpft (Aktiv).");
		builder.add("message.pointportals.destination_obstructed", "Das Zielportal ist blockiert!");
	}
}
