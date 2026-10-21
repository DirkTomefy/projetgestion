package s5.itu.projetgestion.repository.personnel;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.personnel.Personne;

import java.util.Optional;

public interface PersonneRepository extends JpaRepository<Personne, Integer> {
    Optional<Personne> findByCin(String cin);
}