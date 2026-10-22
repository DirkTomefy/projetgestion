package s5.itu.projetgestion.repository.contrat;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.contrat.ContratDetail;

import java.util.List;

public interface ContratDetailRepository extends JpaRepository<ContratDetail, Integer> {
    List<ContratDetail> findByContratId(Integer contratId);
}
