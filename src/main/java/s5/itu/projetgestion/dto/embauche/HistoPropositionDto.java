package s5.itu.projetgestion.dto.embauche;

import java.time.LocalDateTime;

/** utilisateur null : le candidat (page publique) ou une tâche automatique. */
public record HistoPropositionDto(LocalDateTime date, String statut, String utilisateur, String commentaire) {
}
