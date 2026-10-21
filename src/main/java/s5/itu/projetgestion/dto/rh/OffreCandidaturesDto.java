package s5.itu.projetgestion.dto.rh;

import java.time.LocalDate;

public record OffreCandidaturesDto(
        Integer id,
        String reference,
        String etat,
        LocalDate dateLimiteCandidature,
        String postes,
        String unite,
        int nbCandidatures,
        int nbNouvelles) { }
