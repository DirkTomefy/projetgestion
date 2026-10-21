package s5.itu.projetgestion.repository.postes;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.postes.CategorieMetier;

public interface CategorieMetierRepository extends JpaRepository<CategorieMetier, Integer> {
}