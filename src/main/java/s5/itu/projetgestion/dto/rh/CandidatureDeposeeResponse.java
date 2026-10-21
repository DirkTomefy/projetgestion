package s5.itu.projetgestion.dto.rh;

import java.time.LocalDate;

public record CandidatureDeposeeResponse(Integer idCandidature, String referenceOffre,
        LocalDate dateDepot, String message) {
}
