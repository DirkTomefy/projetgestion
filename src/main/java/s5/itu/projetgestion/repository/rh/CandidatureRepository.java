package s5.itu.projetgestion.repository.rh;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import s5.itu.projetgestion.model.rh.Candidature;

import java.util.List;

public interface CandidatureRepository extends JpaRepository<Candidature, Integer> {

    boolean existsByCandidat_IdAndIdOffreEmploi(Integer idCandidat, Integer idOffreEmploi);

    /** Candidatures d'une offre avec compteurs ; {@code etat} nul = tous les états. */
    @Query("""
            SELECT c.id AS id, c.dateDepot AS dateDepot, c.etat AS etat,
                   ca.id AS idCandidat, ca.nom AS nom, ca.prenom AS prenom, ca.email AS email,
                   ca.numero AS telephone, ca.dateNaissance AS dateNaissance,
                   (SELECT count(d) FROM CandidatDetail d WHERE d.candidat = ca) AS nbLignesCv,
                   (SELECT count(dc) FROM DocumentCandidat dc WHERE dc.candidature = c) AS nbPieces
            FROM Candidature c JOIN c.candidat ca
            WHERE c.idOffreEmploi = :idOffre AND (:etat IS NULL OR c.etat = :etat)
            ORDER BY c.dateDepot, c.id
            """)
    List<CandidatureResumeRow> findResumesByOffre(@Param("idOffre") Integer idOffre, @Param("etat") String etat);

    List<Candidature> findByCandidat_IdAndIdNotOrderByDateDepotDescIdDesc(Integer idCandidat, Integer id);

    interface CandidatureResumeRow {
        Integer getId();
        java.time.LocalDate getDateDepot();
        String getEtat();
        Integer getIdCandidat();
        String getNom();
        String getPrenom();
        String getEmail();
        String getTelephone();
        java.time.LocalDate getDateNaissance();
        Long getNbLignesCv();
        Long getNbPieces();
    }
}
