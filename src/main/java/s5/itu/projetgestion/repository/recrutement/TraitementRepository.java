package s5.itu.projetgestion.repository.recrutement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import s5.itu.projetgestion.model.recrutement.Traitement;

import java.util.List;
import java.util.Optional;

public interface TraitementRepository extends JpaRepository<Traitement, Integer> {

    Optional<Traitement> findByCandidature_Id(Integer idCandidature);

    @Query(nativeQuery = true, value = """
            SELECT c.id AS "idCandidature", c.date_depot AS "dateDepot",
                   (SELECT max(h.date_changement) FROM histo_candidature h
                     WHERE h.id_candidature = c.id AND h.etat_apres = 'RETENUE') AS "dateRetenue",
                   o.id AS "idOffre", o.ref AS "referenceOffre", o.id_besoin AS "idBesoin",
                   (SELECT string_agg(DISTINCT p.libelle, ', ' ORDER BY p.libelle)
                      FROM besoin_poste bp JOIN poste p ON p.id = bp.id_poste
                     WHERE bp.id_besoin = o.id_besoin AND (o.id_poste IS NULL OR bp.id_poste = o.id_poste)) AS "postes",
                   ca.id AS "idCandidat", ca.nom AS "nom", ca.prenom AS "prenom",
                   ca.email AS "email", ca.numero AS "telephone"
            FROM candidatures c
            JOIN candidat ca    ON ca.id = c.id_candidat
            JOIN offre_emploi o ON o.id = c.id_offre_emploi
            WHERE c.etat = 'RETENUE'
              AND NOT EXISTS (SELECT 1 FROM proposition_embauche pe
                              WHERE pe.id_candidature = c.id AND pe.id_statut_actuel IN ('PROPOSER', 'ACCEPTER'))
            ORDER BY "dateRetenue", c.id
            """)
    List<CandidatureRetenueRow> findRetenuesSansProposition();

    interface CandidatureRetenueRow {
        Integer getIdCandidature();
        java.time.LocalDate getDateDepot();
        java.time.LocalDateTime getDateRetenue();
        Integer getIdOffre();
        String getReferenceOffre();
        Integer getIdBesoin();
        String getPostes();
        Integer getIdCandidat();
        String getNom();
        String getPrenom();
        String getEmail();
        String getTelephone();
    }
}
