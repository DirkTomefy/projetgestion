package s5.itu.projetgestion.dto.recrutement;

import jakarta.validation.constraints.Size;

public record CommentaireRequest(
        @Size(max = 2000, message = "2000 caractères au maximum")
        String commentaire) {
}
