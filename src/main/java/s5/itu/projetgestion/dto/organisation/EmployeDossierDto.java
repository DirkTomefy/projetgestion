package s5.itu.projetgestion.dto.organisation;

import java.util.List;

import s5.itu.projetgestion.dto.onboarding.OnboardingDto;

/** Dossier complet d'un employé : fiche (identité, poste, contrats), affectations, onboarding, banque, compte. */
public record EmployeDossierDto(EmployeFicheDto fiche, String statut, List<AffectationDto> affectations,
                                List<OnboardingDto> onboardings, List<CoordonneesBancairesDto> banque,
                                String compte) {
}
