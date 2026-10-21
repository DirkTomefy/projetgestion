package s5.itu.projetgestion.repository.embauche;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.embauche.DocumentCandidat;

import java.util.List;

public interface DocumentCandidatRepository extends JpaRepository<DocumentCandidat, Integer> {
    List<DocumentCandidat> findByCandidature_IdOrderByIdAsc(Integer idCandidature);
}
