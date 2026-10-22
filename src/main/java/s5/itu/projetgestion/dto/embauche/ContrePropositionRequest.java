package s5.itu.projetgestion.dto.embauche;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * Contre-proposition de la RH après un refus : une nouvelle valeur pour chaque condition refusée
 * (la même valeur est permise pour la maintenir). Formats : SALAIRE "2800000", DATE_DEBUT "2026-11-02",
 * TYPE_CONTRAT "CDD", texte libre pour HORAIRE, LIEU, CONDITIONS.
 */
public record ContrePropositionRequest(
        @NotEmpty(message = "Indiquez au moins une condition") List<@Valid NouvelleValeur> conditions,
        /** Nouvelle date limite de réponse ; vide : on garde l'actuelle si elle n'est pas dépassée. */
        LocalDate dateLimiteReponse,
        @Size(max = 2000, message = "2000 caractères au plus") String commentaire) {

    public record NouvelleValeur(
            @NotNull(message = "Condition manquante") Integer idCondition,
            @Size(max = 4000, message = "4000 caractères au plus") String valeur) {
    }
}
