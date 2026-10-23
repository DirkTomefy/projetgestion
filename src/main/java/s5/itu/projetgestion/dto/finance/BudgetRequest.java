package s5.itu.projetgestion.dto.finance;

import java.math.BigDecimal;

public record BudgetRequest(Integer idUnite, Integer exercice, BigDecimal montant) {
}
