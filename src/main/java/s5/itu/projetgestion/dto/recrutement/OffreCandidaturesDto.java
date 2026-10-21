package s5.itu.projetgestion.dto.recrutement;

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
