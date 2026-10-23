package s5.itu.projetgestion.dto.organisation;

import java.time.LocalDate;

/**
 * Création (avec idPoste, dateEntree et le contrat en cours : type, fin prévue, salaire mensuel)
 * ou modification de l'identité d'un employé.
 */
public record EmployeRequest(String nom, String prenoms, LocalDate dateNaissance, String adresse, String telephone,
                             String email, String cin, Integer idPoste, LocalDate dateEntree,
                             Integer idTypeContrat, LocalDate dateFinPrevue, java.math.BigDecimal salaire) {
}
