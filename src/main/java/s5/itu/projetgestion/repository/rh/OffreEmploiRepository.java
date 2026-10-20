package s5.itu.projetgestion.repository.rh;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.rh.OffreEmploi;

import java.util.Collection;
import java.util.List;

public interface OffreEmploiRepository extends JpaRepository<OffreEmploi, Integer> {

    boolean existsByRef(String ref);

    boolean existsByIdBesoinAndEtatIn(Integer idBesoin, Collection<String> etats);

    List<OffreEmploi> findAllByOrderByIdDesc();
}
