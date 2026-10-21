package s5.itu.projetgestion.dto.rh;

import java.time.LocalDate;

public record OffrePubliqueDto(
        Integer id,
        String reference,
        LocalDate datePublication,
        LocalDate dateLimiteCandidature,
        String typeContrat,
        String unite,
        String direction,
        String postes,
        int nbPostes,
        boolean ficheDisponible) { }
