package s5.itu.projetgestion.dto.organisation;

import java.time.LocalDate;

public record AffectationDto(Integer id, Integer idPoste, String codePoste, String poste, String unite,
                             LocalDate dateDebut, LocalDate dateFin, boolean enCours) {
}
