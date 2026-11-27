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
		builder.add("guide.pointportals.oval", "Orientación");
		builder.add("guide.pointportals.oval_follow", "Seguir");
		builder.add("guide.pointportals.oval_fixed", "Fija");
		builder.add("guide.pointportals.hint", "Pulsa %s para abrir la guía de portales.");
		builder.add("guide.pointportals.hint_unbound", "Asigna una tecla a la guía de portales en Opciones → Controles.");
		builder.add("key.category.pointportals.portals", "Point-to-Point Portals");
		builder.add("key.pointportals.guide", "Abrir la guía de portales");
		builder.add("guide.pointportals.version", "Versión %s");
		builder.add("guide.pointportals.release", "Lanzamiento %s");
		builder.add("guide.pointportals.unreleased", "En desarrollo");
		builder.add("guide.pointportals.changes", "Novedades de %s");
		builder.add("guide.pointportals.appearance", "Ahora hay una guía y un aviso al entrar por primera vez. Usa Orientación para que el óvalo te siga o quede fijo de este a oeste. La elección se guarda.");
		builder.add("guide.pointportals.cartography", "Mapas de portales");
		builder.add("guide.pointportals.maps", "Usa un mapa lleno en cada base para marcarla. Agáchate y usa el mapa mirando al aire para ver el par. Las copias comparten los marcadores. Un mapa bloqueado los conserva tal como estaban.");
		builder.add("guide.pointportals.pair", "Enlazar portales");
		builder.add("guide.pointportals.pairing", "Coloca dos bases libres, incluso en dimensiones distintas. Usa el Sintonizador de fase en la primera y luego en la segunda. Cada base solo puede tener una pareja.");
		builder.add("guide.pointportals.travel", "Viajar");
		builder.add("guide.pointportals.power", "Alimenta la base de salida con redstone y súbete. Deja dos bloques libres sobre la de destino. Para volver, alimenta esa base, bájate y espera unos segundos antes de volver a subir.");
		builder.add("guide.pointportals.unlink", "Desenlazar portales");
		builder.add("guide.pointportals.unlinking", "Agáchate y usa el Sintonizador de fase vacío en una de las bases. Si ya hay una base seleccionada, el primer uso borra la selección.");
		builder.add(ModBlocks.TELEPORTER, "Teletransportador punto a punto");
		builder.add(ModItems.TUNER, "Sintonizador de fase");
		builder.add("cartography.pointportals.pair", "%s ↔ %s");
		builder.add("cartography.pointportals.no_pairs", "No hay pares de portales registrados.");
		builder.add("cartography.pointportals.unnamed", "Portal (%s, %s, %s)");
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
		builder.add("message.pointportals.label_set", "Etiqueta del portal: %s");
		builder.add("message.pointportals.label_cleared", "Etiqueta del portal eliminada.");
		builder.add("message.pointportals.label_invalid", "Usa una sola línea de hasta %s caracteres.");
		builder.add("message.pointportals.status_unlinked", "El teletransportador no está vinculado. Usa un sintonizador de fase para emparejarlo.");
		builder.add("message.pointportals.status_standby", "Teletransportador vinculado a %s en [%s, %s, %s] (En espera: requiere redstone).");
		builder.add("message.pointportals.status_active", "Teletransportador vinculado a %s en [%s, %s, %s] (Activo).");
		builder.add("message.pointportals.destination_obstructed", "¡El portal de destino está obstruido!");
	}
}
