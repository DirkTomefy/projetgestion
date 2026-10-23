package s5.itu.projetgestion.dto.recrutement;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PlanificationRequest(
        @NotNull(message = "La date prévue est obligatoire")
        LocalDate datePrevue,

        /** Facultatif : à défaut, le poste est celui que la règle du niveau désigne. */
        Integer idPosteResponsable,

        @Size(max = 255, message = "255 caractères au maximum")
        String libelle) {
}
