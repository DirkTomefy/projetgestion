package s5.itu.projetgestion.dto.rh;

import java.time.LocalDate;

public record CandidatureResumeDto(
        Integer id,
        LocalDate dateDepot,
        String etat,
        Integer idCandidat,
        String nom,
        String prenom,
        String email,
        String telephone,
        LocalDate dateNaissance,
        int nbLignesCv,
        int nbPieces) { }
