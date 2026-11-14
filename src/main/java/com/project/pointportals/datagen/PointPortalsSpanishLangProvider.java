package com.project.pointportals.datagen;

import com.project.pointportals.registry.ModBlocks;
import com.project.pointportals.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

public class PointPortalsSpanishLangProvider extends FabricLanguageProvider {
	public PointPortalsSpanishLangProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, "es_es", registries);
	}

	@Override
	public void generateTranslations(HolderLookup.Provider registries, TranslationBuilder builder) {
		builder.add(ModBlocks.TELEPORTER, "Teletransportador punto a punto");
		builder.add(ModItems.TUNER, "Sintonizador de fase");
		builder.add("tooltip.pointportals.tuner_unbound", "Haz clic derecho en un teletransportador sin vincular para iniciar el emparejamiento.");
		builder.add("tooltip.pointportals.tuner_target", "Selección: %s en [%s, %s, %s]");
		builder.add("message.pointportals.first_portal_selected", "Primer portal seleccionado en [%s, %s, %s]. Selecciona el portal de destino.");
		builder.add("message.pointportals.cannot_self_link", "¡No puedes vincular un portal consigo mismo!");
		builder.add("message.pointportals.already_linked", "Este portal ya está vinculado a otro destino.");
		builder.add("message.pointportals.source_portal_missing", "¡No se encontró el primer portal o fue destruido!");
		builder.add("message.pointportals.invalid_dimension", "La dimensión de destino no está accesible en este momento.");
		builder.add("message.pointportals.portals_linked", "¡Portales emparejados! Aliméntalos con redstone para activarlos.");
		builder.add("message.pointportals.portals_unlinked", "Portales desvinculados.");
		builder.add("message.pointportals.tuner_cleared", "Memoria del sintonizador de fase borrada.");
		builder.add("message.pointportals.status_unlinked", "El teletransportador no está vinculado. Usa un sintonizador de fase para emparejarlo.");
		builder.add("message.pointportals.status_standby", "Teletransportador vinculado a %s en [%s, %s, %s] (En espera: requiere redstone).");
		builder.add("message.pointportals.status_active", "Teletransportador vinculado a %s en [%s, %s, %s] (Activo).");
		builder.add("message.pointportals.destination_obstructed", "¡El portal de destino está obstruido!");
	}
}
