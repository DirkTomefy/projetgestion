package s5.itu.projetgestion.repository.rh;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.rh.Candidature;

public interface CandidatureRepository extends JpaRepository<Candidature, Integer> {

    boolean existsByCandidat_IdAndIdOffreEmploi(Integer idCandidat, Integer idOffreEmploi);
}
