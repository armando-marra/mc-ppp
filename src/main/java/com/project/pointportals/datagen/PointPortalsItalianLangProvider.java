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
		builder.add(ModBlocks.TELEPORTER, "Teletrasportatore punto a punto");
		builder.add(ModItems.TUNER, "Sintonizzatore di fase");
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
		builder.add("message.pointportals.status_unlinked", "Il teletrasportatore non è collegato. Usa un sintonizzatore di fase per abbinarlo.");
		builder.add("message.pointportals.status_standby", "Teletrasportatore collegato a %s alle coordinate [%s, %s, %s] (In attesa: richiede redstone).");
		builder.add("message.pointportals.status_active", "Teletrasportatore collegato a %s alle coordinate [%s, %s, %s] (Attivo).");
		builder.add("message.pointportals.destination_obstructed", "Il portale di destinazione è ostruito!");
	}
}
