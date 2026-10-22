package s5.itu.projetgestion.dto.embauche;

import java.time.LocalDate;
import java.util.List;

/**
 * Page publique de réponse du candidat (lien personnel avec jeton).
 * etape : ATTENTE_CANDIDAT (peutRepondre), ATTENTE_RH (réponse reçue, la RH étudie), ACCEPTEE, REFUSEE, EXPIREE.
 */
public record PropositionPubliqueDto(String etape, String message, boolean peutRepondre,
                                     String nom, String prenom, String poste, String unite, String referenceOffre,
                                     LocalDate dateLimiteReponse, List<ConditionDto> conditions) {
}
