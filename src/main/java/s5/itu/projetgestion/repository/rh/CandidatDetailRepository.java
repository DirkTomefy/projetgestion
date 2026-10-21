package s5.itu.projetgestion.repository.rh;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import s5.itu.projetgestion.model.rh.CandidatDetail;

import java.util.List;

public interface CandidatDetailRepository extends JpaRepository<CandidatDetail, Integer> {

    List<CandidatDetail> findByCandidat_Id(Integer idCandidat);

    @Query("""
            SELECT d FROM CandidatDetail d
            WHERE d.candidat.id = :idCandidat
            ORDER BY d.typeInformation.id, d.dateDebut DESC NULLS LAST, d.id
            """)
    List<CandidatDetail> findCvOrdonne(@Param("idCandidat") Integer idCandidat);
}
