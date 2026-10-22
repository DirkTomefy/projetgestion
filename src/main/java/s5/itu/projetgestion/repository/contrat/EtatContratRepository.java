package s5.itu.projetgestion.repository.contrat;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.contrat.EtatContrat;

import java.util.List;

public interface EtatContratRepository extends JpaRepository<EtatContrat, Integer> {
    List<EtatContrat> findByContratIdOrderByDateDesc(Integer contratId);
}
