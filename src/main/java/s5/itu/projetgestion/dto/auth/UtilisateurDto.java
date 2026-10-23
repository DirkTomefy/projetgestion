package s5.itu.projetgestion.dto.auth;

import java.time.LocalDateTime;
import java.util.List;

/** Compte utilisateur tel qu'affiché dans l'administration. */
public record UtilisateurDto(
        Integer id, String login, String nomComplet, String email, String statut, List<String> roles,
        Integer idEmploye, String employe, String matricule, String poste,
        LocalDateTime dateCreation, LocalDateTime derniereConnexion) {
}
