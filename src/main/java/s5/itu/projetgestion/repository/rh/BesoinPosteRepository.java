package s5.itu.projetgestion.repository.rh;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.rh.besoin.BesoinPoste;

public interface BesoinPosteRepository extends JpaRepository<BesoinPoste, Integer> {
    boolean existsByPosteIdAndEtatIn(Integer idPoste, Collection<String> etats);
List<BesoinPoste> findByBesoinId(Integer idBesoin);
}