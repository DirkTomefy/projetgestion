package s5.itu.projetgestion.repository.embauche;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import s5.itu.projetgestion.model.embauche.HistoPropositionEmbauche;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoPropositionEmbaucheRepository extends JpaRepository<HistoPropositionEmbauche, Integer> {

    @Query(nativeQuery = true, value = """
            SELECT h.date AS "date", h.id_statut AS "statut", h.commentaire AS "commentaire",
                   h.id_user AS "idUser", u.login AS "utilisateur"
            FROM histo_proposition_embauche h
            LEFT JOIN "user" u ON u.id = h.id_user
            WHERE h.id_proposition_embauche = :idProposition
            ORDER BY h.date, h.id
            """)
    List<HistoRow> findHistorique(@Param("idProposition") Integer idProposition);

    interface HistoRow {
        LocalDateTime getDate();
        String getStatut();
        String getCommentaire();
        Integer getIdUser();
        String getUtilisateur();
    }
}
