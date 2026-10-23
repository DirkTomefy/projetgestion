package s5.itu.projetgestion.dto.onboarding;

import java.util.List;

public record ModeleOnboardingDto(Integer id, String libelle, String statut, long utilisations, List<Tache> taches) {
    public record Tache(Integer id, Integer ordre, String typeTache, String libelle, Integer idPosteResponsable,
                        String posteResponsable, Integer delaiJours, boolean obligatoire) { }
}
