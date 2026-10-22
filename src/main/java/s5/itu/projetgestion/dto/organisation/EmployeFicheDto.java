package s5.itu.projetgestion.dto.organisation;

import java.time.LocalDate;
import java.util.List;

import s5.itu.projetgestion.dto.contrat.ContratDto;

/** Fiche employé : identité, poste actuel, contrats (du plus récent au plus ancien). */
public record EmployeFicheDto(
    Integer id, String matricule, String nom, String prenoms,
    LocalDate dateNaissance, String adresse, String telephone, String email, String cin,
    Integer idPoste, String codePoste, String poste, String unite, LocalDate dateEntree,
    String superieur,
    List<ContratDto> contrats) {
}
