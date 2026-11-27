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
		builder.add("guide.pointportals.oval", "Ausrichtung");
		builder.add("guide.pointportals.oval_follow", "Folgen");
		builder.add("guide.pointportals.oval_fixed", "Fest");
		builder.add("guide.pointportals.hint", "Drücke %s, um das Portalhandbuch zu öffnen.");
		builder.add("guide.pointportals.hint_unbound", "Weise dem Portalhandbuch unter Optionen → Steuerung eine Taste zu.");
		builder.add("key.category.pointportals.portals", "Point-to-Point Portals");
		builder.add("key.pointportals.guide", "Portalhandbuch öffnen");
		builder.add("guide.pointportals.version", "Version %s");
		builder.add("guide.pointportals.release", "Erschienen am %s");
		builder.add("guide.pointportals.unreleased", "In Entwicklung");
		builder.add("guide.pointportals.changes", "Neu in %s");
		builder.add("guide.pointportals.appearance", "Neu sind dieses Handbuch und der Hinweis beim ersten Betreten. Unter Ausrichtung folgt das Oval dir oder bleibt in Ost-West-Richtung. Die Wahl wird gespeichert.");
		builder.add("guide.pointportals.cartography", "Portale auf Karten");
		builder.add("guide.pointportals.maps", "Benutze eine gefüllte Karte an beiden Portalen, um sie zu markieren. Schau in die Luft und benutze die Karte beim Schleichen, um das Paar zu sehen. Kopien teilen Markierungen. Gesperrte Karten behalten den alten Stand.");
		builder.add("guide.pointportals.pair", "Portale koppeln");
		builder.add("guide.pointportals.pairing", "Platziere zwei freie Portale, auch in verschiedenen Dimensionen. Benutze den Phasenabstimmer erst am einen, dann am anderen Portal. Jedes Portal hat genau einen Partner.");
		builder.add("guide.pointportals.travel", "Reisen");
		builder.add("guide.pointportals.power", "Versorge das Startportal mit Redstone und betritt es. Lass zwei Blöcke über dem Ziel frei. Für die Rückreise braucht das Zielportal Strom. Verlasse es und warte vor dem Betreten einige Sekunden.");
		builder.add("guide.pointportals.unlink", "Kopplung lösen");
		builder.add("guide.pointportals.unlinking", "Benutze den leeren Phasenabstimmer beim Schleichen an einem der Portale. Ist ein Portal ausgewählt, löscht die erste Benutzung die Auswahl.");
		builder.add(ModBlocks.TELEPORTER, "Punkt-zu-Punkt-Teleporter");
		builder.add(ModItems.TUNER, "Phasenabstimmer");
		builder.add("cartography.pointportals.pair", "%s ↔ %s");
		builder.add("cartography.pointportals.no_pairs", "Keine kartierten Portalpaare.");
		builder.add("cartography.pointportals.unnamed", "Portal (%s, %s, %s)");
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
		builder.add("message.pointportals.label_set", "Portalbeschriftung: %s");
		builder.add("message.pointportals.label_cleared", "Portalbeschriftung entfernt.");
		builder.add("message.pointportals.label_invalid", "Verwende eine Zeile mit höchstens %s Zeichen.");
		builder.add("message.pointportals.status_unlinked", "Der Teleporter ist nicht verknüpft. Verwende einen Phasenabstimmer zum Koppeln.");
		builder.add("message.pointportals.status_standby", "Teleporter mit %s bei [%s, %s, %s] verknüpft (Bereit: benötigt Redstone).");
		builder.add("message.pointportals.status_active", "Teleporter mit %s bei [%s, %s, %s] verknüpft (Aktiv).");
		builder.add("message.pointportals.destination_obstructed", "Das Zielportal ist blockiert!");
	}
}
