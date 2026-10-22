package s5.itu.projetgestion.repository.contrat;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.contrat.Contrat;

import java.util.Optional;

public interface ContratRepository extends JpaRepository<Contrat, Integer> {
    Optional<Contrat> findByReference(String reference);
}
