package s5.itu.projetgestion.dto.embauche;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Fiche d'une proposition (RH).
 * etape : ATTENTE_CANDIDAT, ATTENTE_RH (refus partiel : contre-proposer ou retirer), ACCEPTEE, REFUSEE, EXPIREE.
 */
public record PropositionDto(Integer id, String statut, String etape, LocalDate dateLimiteReponse,
                             LocalDateTime dateEnvoi, LocalDateTime dateReponse, String lienReponse,
                             Integer idCandidature, String nom, String prenom, String email, String telephone,
                             Integer idOffre, String referenceOffre,
                             SiegeDto siege, BigDecimal salaireMin, BigDecimal salaireMax,
                             List<ConditionDto> conditions, List<RelanceDto> relances,
                             List<HistoPropositionDto> historique,
                             boolean peutContreProposer, boolean peutRetirer, Integer idContrat) {
}
