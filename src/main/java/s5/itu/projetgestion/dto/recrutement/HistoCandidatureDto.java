package s5.itu.projetgestion.dto.recrutement;

import java.time.LocalDateTime;

public record HistoCandidatureDto(LocalDateTime date, String etatAvant, String etatApres,
                                  String utilisateur, String commentaire) {
}
