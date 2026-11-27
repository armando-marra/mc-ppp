package com.project.pointportals.datagen;

import com.project.pointportals.registry.ModBlocks;
import com.project.pointportals.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

public class PointPortalsItalianLangProvider extends FabricLanguageProvider {
	public PointPortalsItalianLangProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, "it_it", registries);
	}

	@Override
	public void generateTranslations(HolderLookup.Provider registries, TranslationBuilder builder) {
		builder.add("guide.pointportals.oval", "Orientamento");
		builder.add("guide.pointportals.oval_follow", "Segui");
		builder.add("guide.pointportals.oval_fixed", "Fisso");
		builder.add("guide.pointportals.hint", "Premi %s per aprire il pannello e consultare la guida ai portali.");
		builder.add("guide.pointportals.hint_unbound", "Assegna un tasto alla guida ai portali in Opzioni → Comandi.");
		builder.add("key.category.pointportals.portals", "Point-to-Point Portals");
		builder.add("key.pointportals.guide", "Apri la guida ai portali");
		builder.add("guide.pointportals.version", "Versione %s");
		builder.add("guide.pointportals.release", "Rilascio %s");
		builder.add("guide.pointportals.unreleased", "In sviluppo");
		builder.add("guide.pointportals.changes", "Novità della %s");
		builder.add("guide.pointportals.appearance", "Ora hai una guida e un avviso al primo ingresso. Usa Orientamento qui sotto per ruotare l’ovale verso di te o fissarlo da est a ovest. La scelta viene salvata.");
		builder.add("guide.pointportals.cartography", "Mappe dei portali");
		builder.add("guide.pointportals.maps", "Usa una mappa compilata su una pedana per segnarla. Segna entrambe, poi accovacciati e usa la mappa guardando in aria per vedere la coppia. Le copie condividono i segni. Le mappe bloccate li conservano.");
		builder.add("guide.pointportals.pair", "Collegare i portali");
		builder.add("guide.pointportals.pairing", "Posa due pedane libere, anche in dimensioni diverse. Usa il Sintonizzatore di fase sulla prima, poi sulla seconda. Ogni pedana può avere un solo gemello.");
		builder.add("guide.pointportals.travel", "Viaggiare");
		builder.add("guide.pointportals.power", "Alimenta con redstone la pedana di partenza e salici. Lascia due blocchi liberi sopra quella di arrivo. Per tornare, alimenta anche questa pedana, scendi e attendi qualche secondo prima di risalire.");
		builder.add("guide.pointportals.unlink", "Scollegare i portali");
		builder.add("guide.pointportals.unlinking", "Accovacciati e usa il Sintonizzatore di fase vuoto su una delle pedane. Se hai già selezionato una pedana, il primo uso cancella la selezione.");
		builder.add(ModBlocks.TELEPORTER, "Teletrasportatore punto a punto");
		builder.add(ModItems.TUNER, "Sintonizzatore di fase");
		builder.add("cartography.pointportals.pair", "%s ↔ %s");
		builder.add("cartography.pointportals.no_pairs", "Nessuna coppia di portali registrata.");
		builder.add("cartography.pointportals.unnamed", "Portale (%s, %s, %s)");
		builder.add("tooltip.pointportals.tuner_unbound", "Fai clic destro su un teletrasportatore non collegato per iniziare l’abbinamento.");
		builder.add("tooltip.pointportals.tuner_target", "Selezione: %s alle coordinate [%s, %s, %s]");
		builder.add("message.pointportals.first_portal_selected", "Primo portale selezionato alle coordinate [%s, %s, %s]. Seleziona il portale di destinazione.");
		builder.add("message.pointportals.cannot_self_link", "Non puoi collegare un portale a se stesso!");
		builder.add("message.pointportals.already_linked", "Questo portale è già collegato a un’altra destinazione.");
		builder.add("message.pointportals.source_portal_missing", "Il primo portale non è stato trovato oppure è stato distrutto!");
		builder.add("message.pointportals.invalid_dimension", "La dimensione di destinazione non è attualmente accessibile.");
		builder.add("message.pointportals.portals_linked", "Portali abbinati! Alimentali con redstone per attivarli.");
		builder.add("message.pointportals.portals_unlinked", "Collegamento tra i portali rimosso.");
		builder.add("message.pointportals.tuner_cleared", "Memoria del sintonizzatore di fase cancellata.");
		builder.add("message.pointportals.label_set", "Etichetta del portale: %s");
		builder.add("message.pointportals.label_cleared", "Etichetta del portale rimossa.");
		builder.add("message.pointportals.label_invalid", "Usa una sola riga di massimo %s caratteri.");
		builder.add("message.pointportals.status_unlinked", "Il teletrasportatore non è collegato. Usa un sintonizzatore di fase per abbinarlo.");
		builder.add("message.pointportals.status_standby", "Teletrasportatore collegato a %s alle coordinate [%s, %s, %s] (In attesa: richiede redstone).");
		builder.add("message.pointportals.status_active", "Teletrasportatore collegato a %s alle coordinate [%s, %s, %s] (Attivo).");
		builder.add("message.pointportals.destination_obstructed", "Il portale di destinazione è ostruito!");
	}
}
