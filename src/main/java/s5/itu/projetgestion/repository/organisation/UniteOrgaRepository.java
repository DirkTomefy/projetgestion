package s5.itu.projetgestion.repository.organisation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import s5.itu.projetgestion.model.organisation.UniteOrga;

public interface UniteOrgaRepository extends JpaRepository<UniteOrga, Integer> {

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Integer id);

    long countByUniteMereId(Integer idUniteMere);

    @Query("select u from UniteOrga u left join fetch u.uniteMere left join fetch u.posteResponsable order by u.libelle")
    List<UniteOrga> findAllAvecRelations();
}
