package s5.itu.projetgestion.repository.recrutement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import s5.itu.projetgestion.model.recrutement.Evaluation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Integer> {

    /** Les évaluations d'une candidature, dans l'ordre des niveaux. */
    List<Evaluation> findByTraitement_Candidature_IdOrderByNiveau_OrdreAsc(Integer idCandidature);

    /** Un niveau déjà utilisé par une candidature ne peut plus être modifié ni supprimé. */
    boolean existsByNiveau_Id(Integer idNiveau);

    /** Évaluations d'une candidature pour l'affichage : niveau, poste responsable et login de l'évaluateur. */
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

    @Query(nativeQuery = true, value = "SELECT count(*) > 0 FROM poste WHERE id = :idPoste AND etat = 'ACTIF'")
    boolean existsPosteActif(@Param("idPoste") Integer idPoste);

    /** Postes occupés aujourd'hui par un employé (normalement un seul). */
    @Query(nativeQuery = true, value = """
            SELECT a.id_poste FROM affectation a
            WHERE a.id_employe = :idEmploye
              AND a.date_debut <= CURRENT_DATE AND (a.date_fin IS NULL OR a.date_fin >= CURRENT_DATE)
            """)
    List<Integer> findPostesActuels(@Param("idEmploye") Integer idEmploye);

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
