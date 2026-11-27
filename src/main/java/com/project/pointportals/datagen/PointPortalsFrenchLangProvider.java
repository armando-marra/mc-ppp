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
		builder.add("guide.pointportals.oval", "Orientation");
		builder.add("guide.pointportals.oval_follow", "Suivre");
		builder.add("guide.pointportals.oval_fixed", "Fixe");
		builder.add("guide.pointportals.hint", "Appuyez sur %s pour ouvrir le guide des portails.");
		builder.add("guide.pointportals.hint_unbound", "Attribuez une touche au guide des portails dans Options → Contrôles.");
		builder.add("key.category.pointportals.portals", "Point-to-Point Portals");
		builder.add("key.pointportals.guide", "Ouvrir le guide des portails");
		builder.add("guide.pointportals.version", "Version %s");
		builder.add("guide.pointportals.release", "Sortie le %s");
		builder.add("guide.pointportals.unreleased", "En développement");
		builder.add("guide.pointportals.changes", "Nouveautés %s");
		builder.add("guide.pointportals.appearance", "Découvrez le guide et l’avis à la première connexion. Le bouton Orientation permet de suivre votre position ou de fixer l’ovale d’est en ouest. Le choix est enregistré.");
		builder.add("guide.pointportals.cartography", "Cartes des portails");
		builder.add("guide.pointportals.maps", "Utilisez une carte remplie sur chaque socle pour le marquer. Accroupissez-vous et utilisez la carte en visant le ciel pour voir la paire. Les copies partagent les repères. Une carte verrouillée les fige.");
		builder.add("guide.pointportals.pair", "Relier deux portails");
		builder.add("guide.pointportals.pairing", "Posez deux socles libres, même dans des dimensions différentes. Utilisez le Syntoniseur de phase sur le premier, puis sur le second. Chaque socle a un seul partenaire.");
		builder.add("guide.pointportals.travel", "Voyager");
		builder.add("guide.pointportals.power", "Alimentez le socle de départ en redstone et montez dessus. Laissez deux blocs libres au-dessus de l’arrivée. Pour revenir, alimentez ce socle, descendez et attendez quelques secondes avant de remonter.");
		builder.add("guide.pointportals.unlink", "Délier les portails");
		builder.add("guide.pointportals.unlinking", "Accroupissez-vous et utilisez le Syntoniseur de phase vide sur un socle lié. Si un socle est sélectionné, la première utilisation efface la sélection.");
		builder.add(ModBlocks.TELEPORTER, "Téléporteur point à point");
		builder.add(ModItems.TUNER, "Syntoniseur de phase");
		builder.add("cartography.pointportals.pair", "%s ↔ %s");
		builder.add("cartography.pointportals.no_pairs", "Aucune paire de portails cartographiée.");
		builder.add("cartography.pointportals.unnamed", "Portail (%s, %s, %s)");
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
