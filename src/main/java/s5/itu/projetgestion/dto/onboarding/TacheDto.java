package s5.itu.projetgestion.dto.onboarding;

import java.time.LocalDate;

/** Tâche d'onboarding, avec le contexte (employé) pour la liste « Mes tâches ». */
public record TacheDto(Integer id, Integer idOnboarding, Integer ordre, String typeTache, String libelle,
                       Integer idPosteResponsable, String posteResponsable, boolean obligatoire, String statut,
                       LocalDate dateEcheance, boolean enRetard, String realisateur, LocalDate dateRealisation,
                       String commentaire, String employe, String matricule, String posteEmploye, boolean modifiable) {
}
