package s5.itu.projetgestion.dto.embauche;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Réponse du candidat (page publique) : accepter ou refuser chaque condition en attente. */
public record ReponseCandidatRequest(
        @NotEmpty(message = "Répondez à chaque condition") List<@Valid ReponseCondition> reponses) {

    public record ReponseCondition(
            @NotNull(message = "Condition manquante") Integer idCondition,
            @NotNull(message = "Indiquez si vous acceptez cette condition") Boolean accepte,
            /** Motif ou souhait du candidat en cas de refus. */
            @Size(max = 2000, message = "2000 caractères au plus") String commentaire) {
    }
}
