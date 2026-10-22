package s5.itu.projetgestion.dto.recrutement;

import java.util.List;

public record MesEvaluationsDto(boolean relieAEmploye, List<EvaluationAVenirDto> evaluations) {
}
