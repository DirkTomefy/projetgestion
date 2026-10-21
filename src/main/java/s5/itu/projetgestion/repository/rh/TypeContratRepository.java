package s5.itu.projetgestion.repository.rh;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.rh.TypeContrat;

import java.util.Optional;

public interface TypeContratRepository extends JpaRepository<TypeContrat, Integer> {
	Optional<TypeContrat> findByLibelleIgnoreCase(String libelle);
}
