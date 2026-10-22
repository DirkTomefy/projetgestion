package s5.itu.projetgestion.dto.embauche;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Saisie d'une proposition par la RH, à partir d'une candidature RETENUE. */
public record PropositionRequest(
        @NotNull(message = "La candidature est obligatoire") Integer idCandidature,
        @NotNull(message = "Choisissez le poste (siège) proposé") Integer idBesoinPoste,
        @NotNull(message = "Le salaire est obligatoire") BigDecimal salaire,
        @NotNull(message = "La date de début est obligatoire") LocalDate dateDebut,
        @Size(max = 1000, message = "1000 caractères au plus") String horaire,
        @Size(max = 1000, message = "1000 caractères au plus") String lieu,
        @Size(max = 4000, message = "4000 caractères au plus") String conditionsTravail,
        /** Facultatif : type de contrat de l'offre, sinon CE. */
        Integer idTypeContrat,
        @NotNull(message = "La date limite de réponse est obligatoire") LocalDate dateLimiteReponse,
        @Size(max = 2000, message = "2000 caractères au plus") String commentaire) {
}
