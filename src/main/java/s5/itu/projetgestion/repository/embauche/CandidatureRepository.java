package s5.itu.projetgestion.repository.embauche;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.embauche.Candidature;

import java.util.Optional;

public interface CandidatureRepository extends JpaRepository<Candidature, Integer> {
    Optional<Candidature> findByCandidatIdAndOffreEmploiId(Integer candidatId, Integer offreEmploiId);
}
