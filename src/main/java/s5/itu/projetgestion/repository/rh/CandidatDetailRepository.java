package s5.itu.projetgestion.repository.rh;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.rh.CandidatDetail;

import java.util.List;

public interface CandidatDetailRepository extends JpaRepository<CandidatDetail, Integer> {

    List<CandidatDetail> findByCandidat_Id(Integer idCandidat);
}
