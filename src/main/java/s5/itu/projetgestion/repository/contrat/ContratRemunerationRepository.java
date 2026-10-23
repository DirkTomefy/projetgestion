package s5.itu.projetgestion.repository.contrat;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.contrat.ContratRemuneration;

import java.util.List;

public interface ContratRemunerationRepository extends JpaRepository<ContratRemuneration, Integer> {
    List<ContratRemuneration> findByContratIdOrderByDateEffetDesc(Integer contratId);
}
