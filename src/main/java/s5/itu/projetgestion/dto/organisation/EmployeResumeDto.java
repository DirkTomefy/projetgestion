package s5.itu.projetgestion.dto.organisation;

import java.time.LocalDate;

/** Ligne de la liste du personnel. */
public record EmployeResumeDto(
    Integer id, String matricule, String nom, String prenoms,
    String poste, String unite, LocalDate dateEntree) {
}
