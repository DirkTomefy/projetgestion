package s5.itu.projetgestion.dto.embauche;

import jakarta.validation.constraints.Size;

/** Motif libre : retrait d'une proposition par la RH, ou refus global par le candidat. */
public record MotifRequest(@Size(max = 2000, message = "2000 caractères au plus") String commentaire) {
}
