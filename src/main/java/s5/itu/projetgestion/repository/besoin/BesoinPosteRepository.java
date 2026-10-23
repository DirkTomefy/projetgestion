package s5.itu.projetgestion.repository.besoin;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.besoin.BesoinPoste;

public interface BesoinPosteRepository extends JpaRepository<BesoinPoste, Integer> {
    boolean existsByPosteIdAndEtatIn(Integer idPoste, Collection<String> etats);
List<BesoinPoste> findByBesoinId(Integer idBesoin);

    /** Ligne de besoin la plus récente qui porte sur ce poste (besoin d'origine d'un poste créé par un besoin). */
    @org.springframework.data.jpa.repository.Query("""
        select bp from BesoinPoste bp join fetch bp.besoin b
        where bp.poste.id = :idPoste order by b.id desc limit 1
        """)
    java.util.Optional<BesoinPoste> findDernierParPoste(@org.springframework.data.repository.query.Param("idPoste") Integer idPoste);
}
