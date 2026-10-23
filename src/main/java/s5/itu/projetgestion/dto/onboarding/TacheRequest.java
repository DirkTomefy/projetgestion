package s5.itu.projetgestion.dto.onboarding;

/** statut : FAIT, NON_APPLICABLE ou A_FAIRE (réouverture). */
public record TacheRequest(String statut, String commentaire) {
}
