package s5.itu.projetgestion.dto.onboarding;

import java.util.List;

/** Tâches : id renseigné = tâche existante modifiée ; sans id = nouvelle ; absente = supprimée (si jamais utilisée). */
public record ModeleOnboardingRequest(String libelle, String statut, List<Tache> taches) {
    public record Tache(Integer id, String typeTache, String libelle, Integer idPosteResponsable, Integer delaiJours, Boolean obligatoire) { }
}
