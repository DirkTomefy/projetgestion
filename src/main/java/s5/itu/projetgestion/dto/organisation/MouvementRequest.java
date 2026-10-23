package s5.itu.projetgestion.dto.organisation;

import java.time.LocalDate;

/** Mutation (idPoste) ou départ (motif : DEMISSION, LICENCIEMENT, FIN_CONTRAT, RETRAITE, AUTRE). */
public record MouvementRequest(Integer idPoste, LocalDate date, String motif, String commentaire) {
}
