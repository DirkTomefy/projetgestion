package s5.itu.projetgestion.dto.contrat;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Décision sur une demande. Les champs facultatifs ne servent qu'à la RH, qui peut ajuster la demande en l'instruisant. */
public record DecisionContratRequest(Boolean accepter, String commentaire,
                                     BigDecimal salaire, LocalDate dateFinPrevue, LocalDate dateEffet) {
}
