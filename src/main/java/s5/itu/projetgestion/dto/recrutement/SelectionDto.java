package s5.itu.projetgestion.dto.recrutement;

import java.util.List;

public record SelectionDto(Integer idCandidature, String etat, List<EvaluationDto> evaluations,
                           List<HistoCandidatureDto> historique,
                           boolean peutPreselectionner, boolean peutRefuser) {
}
