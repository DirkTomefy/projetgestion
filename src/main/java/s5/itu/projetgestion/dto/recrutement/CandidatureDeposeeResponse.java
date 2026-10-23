package s5.itu.projetgestion.dto.recrutement;

import java.time.LocalDate;

public record CandidatureDeposeeResponse(Integer idCandidature, String referenceOffre,
        LocalDate dateDepot, String message) {
}
