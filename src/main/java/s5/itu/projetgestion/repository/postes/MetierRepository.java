package s5.itu.projetgestion.repository.postes;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.postes.Metier;

public interface MetierRepository extends JpaRepository<Metier, Integer> {
    boolean existsByLibelleIgnoreCaseAndCategorieId(String libelle, Integer categorieId);
}