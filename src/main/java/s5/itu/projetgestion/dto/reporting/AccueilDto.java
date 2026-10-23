package s5.itu.projetgestion.dto.reporting;

import java.util.List;

/** Page d'accueil de l'utilisateur connecté : ce qui l'attend. */
public record AccueilDto(String nom, List<String> roles, String poste, String unite,
                         long validationsAFaire, long evaluationsAVenir, long tachesOnboarding, long notificationsNonLues,
                         long mesBesoinsEnCours, long inscriptionsAValider, List<String> delegationsRecues,
                         long budgetsAValider, long grillesAValider) {
}
