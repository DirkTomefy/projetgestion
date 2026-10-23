package s5.itu.projetgestion.dto.onboarding;

import java.time.LocalDate;
import java.util.List;

/** Onboarding d'un employé ; taches vide dans la liste, rempli dans la fiche. */
public record OnboardingDto(Integer id, Integer idEmploye, String employe, String matricule, String poste, String unite,
                            Integer idContrat, String contrat, String modele, String statut,
                            LocalDate dateDebut, LocalDate dateFin, int faites, int total, int enRetard,
                            List<TacheDto> taches) {
}
