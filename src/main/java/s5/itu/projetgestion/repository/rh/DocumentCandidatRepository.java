package s5.itu.projetgestion.repository.rh;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.rh.DocumentCandidat;

import java.util.List;

public interface DocumentCandidatRepository extends JpaRepository<DocumentCandidat, Integer> {

    List<DocumentCandidat> findByCandidature_IdOrderByIdAsc(Integer idCandidature);
}
