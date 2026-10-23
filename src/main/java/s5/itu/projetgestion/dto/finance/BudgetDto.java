package s5.itu.projetgestion.dto.finance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Budget annuel d'une unité, avec la masse salariale annuelle engagée (contrats en cours × 12)
 * et le coût estimé des besoins en cours de recrutement.
 * montant = montant validé par le DG (utilisé par le contrôle budgétaire) ; montantPropose = en attente de décision.
 * statut : A_VALIDER, VALIDE, REFUSE.
 */
public record BudgetDto(Integer id, Integer idUnite, String unite, Integer exercice, BigDecimal montant,
                        BigDecimal masseSalariale, BigDecimal besoinsEnCours, BigDecimal disponible,
                        BigDecimal montantPropose, String statut, String proposePar, String decidePar,
                        LocalDateTime dateDecision, String commentaire) {
}
