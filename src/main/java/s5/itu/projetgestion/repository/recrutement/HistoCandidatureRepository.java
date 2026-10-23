package s5.itu.projetgestion.repository.recrutement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import s5.itu.projetgestion.model.recrutement.HistoCandidature;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoCandidatureRepository extends JpaRepository<HistoCandidature, Integer> {

    @Query("""
            SELECT h.dateChangement AS dateChangement, h.etatAvant AS etatAvant, h.etatApres AS etatApres,
                   u.login AS utilisateur, h.commentaire AS commentaire
            FROM HistoCandidature h LEFT JOIN User u ON u.id = h.idUser
            WHERE h.candidature.id = :idCandidature
            ORDER BY h.dateChangement, h.id
            """)
    List<HistoRow> findHistorique(@Param("idCandidature") Integer idCandidature);

    interface HistoRow {
        LocalDateTime getDateChangement();
        String getEtatAvant();
        String getEtatApres();
        String getUtilisateur();
        String getCommentaire();
    }
}
