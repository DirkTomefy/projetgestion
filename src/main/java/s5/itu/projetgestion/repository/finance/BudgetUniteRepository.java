package s5.itu.projetgestion.repository.finance;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import s5.itu.projetgestion.model.finance.BudgetUnite;
import s5.itu.projetgestion.model.organisation.UniteOrga;

public interface BudgetUniteRepository extends JpaRepository<BudgetUnite, Integer>{

    /** @deprecated plusieurs exercices par unité : utiliser findByUniteOrgaIdAndExercice. */
    @Deprecated
    @Query("select b from BudgetUnite b where b.uniteOrga = :unite order by b.exercice desc limit 1")
    BudgetUnite findByUniteOrga(@Param("unite") UniteOrga uniteOrga);

    Optional<BudgetUnite> findByUniteOrgaIdAndExercice(Integer idUnite, Integer exercice);

    long countByStatut(String statut);

    @Query("select b from BudgetUnite b join fetch b.uniteOrga u where b.exercice = :exercice order by b.exercice desc, u.libelle")
    List<BudgetUnite> rechercher(@Param("exercice") Integer exercice);
}
