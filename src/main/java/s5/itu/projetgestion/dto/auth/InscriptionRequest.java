package s5.itu.projetgestion.dto.auth;

/** Inscription publique : le compte est créé EN_ATTENTE jusqu'à sa validation par un administrateur. */
public record InscriptionRequest(String login, String nomComplet, String email, String motDePasse,
                                 String confirmation, String matricule) {
}
