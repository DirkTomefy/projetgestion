package s5.itu.projetgestion.dto.auth;

import java.util.List;

/** Création ou modification d'un compte par l'administrateur (motDePasse ignoré en modification). */
public record UtilisateurRequest(String login, String nomComplet, String email, String motDePasse,
                                 List<String> roles, Integer idEmploye, String statut) {
}
