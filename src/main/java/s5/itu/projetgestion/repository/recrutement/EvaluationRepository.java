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
