package s5.itu.projetgestion.dto.contrat;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Création d'une demande : motifType = RENOUVELLEMENT, TRANSFORMATION ou AUGMENTATION. */
public record DemandeContratRequest(Integer idContrat, String motifType, Integer idTypeContrat,
                                    LocalDate dateEffet, LocalDate dateFinPrevue, BigDecimal salaire, String motif) {
}
