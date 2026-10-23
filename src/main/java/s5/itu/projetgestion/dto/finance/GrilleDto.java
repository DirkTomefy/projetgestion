package s5.itu.projetgestion.dto.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Grille salariale. statut (décision) : A_VALIDER, VALIDEE, REFUSEE ;
 * etat (application) : EN_VIGUEUR, A_VENIR, ECHUE pour une grille validée, sinon A_VALIDER ou REFUSEE.
 */
public record GrilleDto(Integer id, LocalDate dateEffectif, LocalDate dateFin, String etat, List<Ligne> lignes,
                        String statut, String proposePar, String decidePar, LocalDateTime dateDecision, String commentaire) {
    public record Ligne(Integer id, Integer idCategorie, String categorie, Integer idEchelon, String echelon,
                        BigDecimal montantMin, BigDecimal montantMax) { }
}
