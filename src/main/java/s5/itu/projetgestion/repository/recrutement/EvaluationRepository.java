package s5.itu.projetgestion.repository.recrutement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import s5.itu.projetgestion.model.recrutement.Evaluation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Integer> {

    List<Evaluation> findByTraitement_Candidature_IdOrderByNiveau_OrdreAsc(Integer idCandidature);

    boolean existsByNiveau_Id(Integer idNiveau);

    @Query(nativeQuery = true, value = """
            SELECT td.id AS "id", w.ordre AS "ordre", w.type_evaluation AS "typeEvaluation", w.score_min AS "scoreMin",
                   td.date_prevue AS "datePrevue", td.date_realisee AS "dateRealisee", td.libelle AS "libelle",
                   td.id_poste_responsable AS "idPosteResponsable", p.libelle AS "posteResponsable",
                   u.login AS "evaluateur", td.score AS "score", td.decision AS "decision", td.commentaire AS "commentaire"
            FROM traitement_detail td
            JOIN traitement t            ON t.id = td.id_traitement
            JOIN workflow_candidature w  ON w.id = td.id_workflow_traitement
            LEFT JOIN poste p            ON p.id = td.id_poste_responsable
            LEFT JOIN "user" u           ON u.id = td.id_user
            WHERE t.id_candidature = :idCandidature
            ORDER BY w.ordre
            """)
    List<EvaluationRow> findLignes(@Param("idCandidature") Integer idCandidature);


    /** Postes actifs, proposés comme responsables d'une évaluation. */
    @Query(nativeQuery = true, value = """
            SELECT p.id AS "id", p.libelle AS "libelle", u.libelle AS "unite"
            FROM poste p JOIN unite_orga u ON u.id = p.id_unite_orga
            WHERE p.etat = 'ACTIF'
            ORDER BY u.libelle, p.libelle
            """)
    List<PosteOptionRow> findPostesActifs();

    /** Poste à pourvoir de l'offre (à défaut, l'unique poste de son besoin). */
    @Query(nativeQuery = true, value = """
            SELECT COALESCE(o.id_poste, (SELECT MIN(bp.id_poste) FROM besoin_poste bp WHERE bp.id_besoin = o.id_besoin))
            FROM offre_emploi o WHERE o.id = :idOffre
            """)
    Integer findPosteAPourvoir(@Param("idOffre") Integer idOffre);

    /** Supérieur direct actif d'un poste. */
    @Query(nativeQuery = true, value = """
            SELECT s.id FROM poste p JOIN poste s ON s.id = p.id_superieur_direct
            WHERE p.id = :idPoste AND s.etat = 'ACTIF'
            """)
    Integer findSuperieurActif(@Param("idPoste") Integer idPoste);

    /** Poste responsable de l'unité du poste, ou à défaut de la première unité parente qui en a un (autre que le poste lui-même). */
    @Query(nativeQuery = true, value = """
            WITH RECURSIVE u AS (
                SELECT uo.id, uo.id_unite_mere, uo.id_poste_responsable, 0 AS niv
                FROM unite_orga uo JOIN poste p ON p.id_unite_orga = uo.id WHERE p.id = :idPoste
                UNION ALL
                SELECT m.id, m.id_unite_mere, m.id_poste_responsable, u.niv + 1
                FROM unite_orga m JOIN u ON m.id = u.id_unite_mere
            )
            SELECT u.id_poste_responsable FROM u JOIN poste r ON r.id = u.id_poste_responsable
            WHERE r.etat = 'ACTIF' AND u.id_poste_responsable <> :idPoste
            ORDER BY u.niv LIMIT 1
            """)
    Integer findResponsableUnite(@Param("idPoste") Integer idPoste);

    @Query(nativeQuery = true, value = "SELECT libelle FROM poste WHERE id = :idPoste")
    String findLibellePoste(@Param("idPoste") Integer idPoste);

    @Query(nativeQuery = true, value = "SELECT count(*) > 0 FROM poste WHERE id = :idPoste AND etat = 'ACTIF'")
    boolean existsPosteActif(@Param("idPoste") Integer idPoste);

    /** Postes occupés aujourd'hui par un employé (normalement un seul). */
    @Query(nativeQuery = true, value = """
            SELECT a.id_poste FROM affectation a
            WHERE a.id_employe = :idEmploye
              AND a.date_debut <= CURRENT_DATE AND (a.date_fin IS NULL OR a.date_fin >= CURRENT_DATE)
            """)
    List<Integer> findPostesActuels(@Param("idEmploye") Integer idEmploye);


    /**
     * Évaluations pas encore passées dont l'un des postes donnés est responsable.
     * saisissable = c'est le niveau en cours (tous les niveaux précédents sont réussis).
     */
    @Query(nativeQuery = true, value = """
            SELECT td.id AS "id", w.ordre AS "ordre", w.type_evaluation AS "typeEvaluation", w.score_min AS "scoreMin",
                   td.date_prevue AS "datePrevue", td.libelle AS "libelle", p.libelle AS "posteResponsable",
                   c.id AS "idCandidature", ca.nom AS "nom", ca.prenom AS "prenom", o.ref AS "referenceOffre",
                   NOT EXISTS (
                       SELECT 1 FROM traitement_detail td2
                       JOIN workflow_candidature w2 ON w2.id = td2.id_workflow_traitement
                       WHERE td2.id_traitement = td.id_traitement AND w2.ordre < w.ordre
                         AND td2.decision IS DISTINCT FROM 'ADMIS'
                   ) AS "saisissable"
            FROM traitement_detail td
            JOIN traitement t           ON t.id = td.id_traitement
            JOIN candidatures c         ON c.id = t.id_candidature
            JOIN candidat ca            ON ca.id = c.id_candidat
            JOIN offre_emploi o         ON o.id = c.id_offre_emploi
            JOIN workflow_candidature w ON w.id = td.id_workflow_traitement
            LEFT JOIN poste p           ON p.id = td.id_poste_responsable
            WHERE c.etat = 'PRESELECTIONNER' AND td.decision IS NULL AND td.id_poste_responsable IN (:postes)
            ORDER BY td.date_prevue NULLS LAST, c.id, w.ordre
            """)
    List<EvaluationAVenirRow> findAVenirPourPostes(@Param("postes") List<Integer> postes);

    interface EvaluationAVenirRow {
        Integer getId();
        Integer getOrdre();
        String getTypeEvaluation();
        BigDecimal getScoreMin();
        LocalDate getDatePrevue();
        String getLibelle();
        String getPosteResponsable();
        Integer getIdCandidature();
        String getNom();
        String getPrenom();
        String getReferenceOffre();
        Boolean getSaisissable();
    }

    interface PosteOptionRow {
        Integer getId();
        String getLibelle();
        String getUnite();
    }

    interface EvaluationRow {
        Integer getId();
        Integer getOrdre();
        String getTypeEvaluation();
        BigDecimal getScoreMin();
        LocalDate getDatePrevue();
        LocalDate getDateRealisee();
        String getLibelle();
        Integer getIdPosteResponsable();
        String getPosteResponsable();
        String getEvaluateur();
        BigDecimal getScore();
        String getDecision();
        String getCommentaire();
    }
}
