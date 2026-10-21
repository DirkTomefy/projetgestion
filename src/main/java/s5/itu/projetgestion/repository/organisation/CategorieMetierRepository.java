package s5.itu.projetgestion.repository.organisation;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.organisation.CategorieMetier;

public interface CategorieMetierRepository extends JpaRepository<CategorieMetier, Integer> {
}