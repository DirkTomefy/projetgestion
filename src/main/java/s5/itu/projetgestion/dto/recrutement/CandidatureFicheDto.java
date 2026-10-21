package s5.itu.projetgestion.dto.recrutement;

import java.time.LocalDate;
import java.util.List;

public record CandidatureFicheDto(
        Integer id,
        LocalDate dateDepot,
        String etat,
        Integer idOffre,
        String referenceOffre,
        Candidat candidat,
        List<LigneCv> lignesCv,
        List<Piece> pieces,
        List<AutreCandidature> autresCandidatures) {

    public record Candidat(Integer id, String nom, String prenom, LocalDate dateNaissance,
                           String adresse, String email, String telephone) { }

    public record LigneCv(String type, String libelle, LocalDate dateDebut, LocalDate dateFin) { }

    public record Piece(Integer idDocument, String type, String nomFichier) { }

    public record AutreCandidature(Integer id, Integer idOffre, String referenceOffre,
                                   LocalDate dateDepot, String etat) { }
}
