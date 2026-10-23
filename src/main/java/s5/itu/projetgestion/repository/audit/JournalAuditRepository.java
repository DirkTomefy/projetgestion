package s5.itu.projetgestion.repository.audit;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import s5.itu.projetgestion.model.audit.JournalAudit;

public interface JournalAuditRepository extends JpaRepository<JournalAudit, Integer> {

    /** Filtres : '' pour le texte (pas de filtre), bornes de dates toujours renseignées ; succes : 0 = tous, 1 = réussis, 2 = échecs. */
    @Query("""
        select j from JournalAudit j
        where (:q = '' or lower(concat(coalesce(j.login, ''), ' ', j.action, ' ', j.chemin)) like concat('%', :q, '%'))
          and j.dateAction >= :debut
          and j.dateAction < :fin
          and (:succes = 0 or (:succes = 1 and j.statutHttp < 400) or (:succes = 2 and j.statutHttp >= 400))
        order by j.dateAction desc, j.id desc
        """)
    List<JournalAudit> rechercher(@Param("q") String q, @Param("debut") LocalDateTime debut,
                                  @Param("fin") LocalDateTime fin, @Param("succes") int succes, Pageable page);
}
