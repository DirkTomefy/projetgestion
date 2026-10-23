package s5.itu.projetgestion.repository.contrat;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.contrat.EtatContrat;

import java.util.List;

public interface EtatContratRepository extends JpaRepository<EtatContrat, Integer> {
    /** Historique d'un contrat, dans l'ordre des changements. */
    List<EtatContrat> findByContratIdOrderByIdAsc(Integer contratId);
}
