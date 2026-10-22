package s5.itu.projetgestion.dto.embauche;

import s5.itu.projetgestion.dto.commun.OptionDto;

import java.time.LocalDate;
import java.util.List;

/** Données pour saisir une proposition : la candidature, les sièges possibles et des valeurs par défaut. */
public record FormulairePropositionDto(Integer idCandidature, String nom, String prenom, String email,
                                       Integer idOffre, String referenceOffre,
                                       List<SiegeDto> sieges, List<OptionDto> typesContrat,
                                       Integer idTypeContratParDefaut, LocalDate dateDebutParDefaut,
                                       LocalDate dateLimiteReponseParDefaut, String lieuParDefaut,
                                       List<PropositionResumeDto> propositionsPrecedentes) {
}
