package s5.itu.projetgestion.dto.recrutement;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EvaluationDto(Integer id, Integer ordre, String typeEvaluation, BigDecimal scoreMin,
                            LocalDate datePrevue, LocalDate dateRealisee, String libelle,
                            Integer idPosteResponsable, String posteResponsable, String evaluateur,
                            BigDecimal score, String decision, String commentaire, String statut) {
}
