package s5.itu.projetgestion.dto.recrutement;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EvaluationAVenirDto(Integer id, Integer ordre, String typeEvaluation, BigDecimal scoreMin,
                                  LocalDate datePrevue, String libelle, String posteResponsable,
                                  Integer idCandidature, String candidat, String referenceOffre, boolean aSaisir) {
}
