package s5.itu.projetgestion.repository.recrutement;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.recrutement.Traitement;

import java.util.Optional;

public interface TraitementRepository extends JpaRepository<Traitement, Integer> {

    Optional<Traitement> findByCandidature_Id(Integer idCandidature);
}
