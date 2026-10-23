package s5.itu.projetgestion.dto.recrutement;

import java.math.BigDecimal;

public record NiveauSelectionDto(Integer id, Integer ordre, String typeEvaluation, BigDecimal scoreMin,
                                 Integer idOffreEmploi, boolean utilise,
                                 String responsableRegle, Integer idPosteResponsable, String posteFixe,
                                 String responsableApercu) {
}
