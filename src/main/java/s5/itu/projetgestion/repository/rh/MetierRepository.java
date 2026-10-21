package s5.itu.projetgestion.repository.rh;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.rh.organisation.Metier;

public interface MetierRepository extends JpaRepository<Metier, Integer> {
    boolean existsByLibelleIgnoreCaseAndCategorieId(String libelle, Integer idCategorie);
}
