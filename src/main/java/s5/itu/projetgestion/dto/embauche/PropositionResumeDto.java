package s5.itu.projetgestion.dto.embauche;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PropositionResumeDto(Integer id, String statut, String etape, LocalDate dateLimiteReponse,
                                   LocalDateTime dateEnvoi, LocalDateTime dateReponse,
                                   Integer idCandidature, String nom, String prenom, String email,
                                   Integer idOffre, String referenceOffre, String poste, String salaire,
                                   int nbConditionsEnAttente, int nbConditionsRefusees, Integer idContrat) {
}
