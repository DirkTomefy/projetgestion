package s5.itu.projetgestion.repository.finance;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.finance.BudgetUnite;
import s5.itu.projetgestion.model.postes.UniteOrga;

public interface BudgetUniteRepository extends JpaRepository<BudgetUnite, Integer>{
    public BudgetUnite findByUniteOrga(UniteOrga uniteOrga);
}
