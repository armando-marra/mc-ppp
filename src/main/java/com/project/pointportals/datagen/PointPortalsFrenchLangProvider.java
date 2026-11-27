package com.project.pointportals.datagen;

import com.project.pointportals.registry.ModBlocks;
import com.project.pointportals.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

public class PointPortalsFrenchLangProvider extends FabricLanguageProvider {
	public PointPortalsFrenchLangProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, "fr_fr", registries);
	}

	@Override
	public void generateTranslations(HolderLookup.Provider registries, TranslationBuilder builder) {
		builder.add(ModBlocks.TELEPORTER, "Téléporteur point à point");
		builder.add(ModItems.TUNER, "Syntoniseur de phase");
		builder.add("tooltip.pointportals.tuner_unbound", "Faites un clic droit sur un téléporteur non lié pour commencer l’association.");
		builder.add("tooltip.pointportals.tuner_target", "Sélection : %s aux coordonnées [%s, %s, %s]");
		builder.add("message.pointportals.first_portal_selected", "Premier portail sélectionné aux coordonnées [%s, %s, %s]. Sélectionnez le portail de destination.");
		builder.add("message.pointportals.cannot_self_link", "Impossible de relier un portail à lui-même !");
		builder.add("message.pointportals.already_linked", "Ce portail est déjà lié à une autre destination.");
		builder.add("message.pointportals.source_portal_missing", "Le premier portail est introuvable ou a été détruit !");
		builder.add("message.pointportals.invalid_dimension", "La dimension de destination est actuellement inaccessible.");
		builder.add("message.pointportals.portals_linked", "Portails associés ! Alimentez-les en redstone pour les activer.");
		builder.add("message.pointportals.portals_unlinked", "Liaison entre les portails supprimée.");
		builder.add("message.pointportals.tuner_cleared", "Mémoire du syntoniseur de phase effacée.");
		builder.add("message.pointportals.label_set", "Inscription du portail : %s");
		builder.add("message.pointportals.label_cleared", "Inscription du portail effacée.");
		builder.add("message.pointportals.label_invalid", "Utilisez une seule ligne de %s caractères maximum.");
		builder.add("message.pointportals.status_unlinked", "Le téléporteur n’est pas lié. Utilisez un syntoniseur de phase pour l’associer.");
		builder.add("message.pointportals.status_standby", "Téléporteur lié à %s aux coordonnées [%s, %s, %s] (En veille : nécessite de la redstone).");
		builder.add("message.pointportals.status_active", "Téléporteur lié à %s aux coordonnées [%s, %s, %s] (Actif).");
		builder.add("message.pointportals.destination_obstructed", "Le portail de destination est obstrué !");
	}
}
