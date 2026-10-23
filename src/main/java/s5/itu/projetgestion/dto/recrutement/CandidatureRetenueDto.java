package s5.itu.projetgestion.dto.recrutement;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CandidatureRetenueDto(Integer idCandidature, LocalDate dateDepot, LocalDateTime dateRetenue,
                                    Integer idOffre, String referenceOffre, Integer idBesoin, String postes,
                                    Integer idCandidat, String nom, String prenom, String email, String telephone) {
}
