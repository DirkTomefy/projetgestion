package s5.itu.projetgestion.repository.recrutement;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.recrutement.Personne;

import java.util.Optional;

public interface PersonneRepository extends JpaRepository<Personne, Integer> {
    Optional<Personne> findByCin(String cin);
}