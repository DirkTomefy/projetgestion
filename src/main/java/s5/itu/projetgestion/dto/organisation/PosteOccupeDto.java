package s5.itu.projetgestion.dto.organisation;

import java.time.LocalDate;

public record PosteOccupeDto(Integer idPoste, String poste, Integer idEmploye,
                             String employe, String matricule, LocalDate dateDepart) {}