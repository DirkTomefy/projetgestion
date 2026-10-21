package s5.itu.projetgestion.dto.recrutement;

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
