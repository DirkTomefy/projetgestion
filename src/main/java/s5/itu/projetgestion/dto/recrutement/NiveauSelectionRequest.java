package s5.itu.projetgestion.dto.recrutement;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record NiveauSelectionRequest(
        @NotNull(message = "L'ordre est obligatoire")
        @Min(value = 1, message = "L'ordre commence à 1")
        Integer ordre,

        @NotBlank(message = "Le type est obligatoire")
        String typeEvaluation,

        @DecimalMin(value = "0", message = "Le score minimum est entre 0 et 20")
        @DecimalMax(value = "20", message = "Le score minimum est entre 0 et 20")
        BigDecimal scoreMin,

        Integer idOffreEmploi,

        /** SUPERIEUR, RESPONSABLE_UNITE, POSTE_FIXE ; absent = choix manuel à la planification. */
        String responsableRegle,

        /** Poste du niveau quand la règle est POSTE_FIXE. */
        Integer idPosteResponsable) {
}
