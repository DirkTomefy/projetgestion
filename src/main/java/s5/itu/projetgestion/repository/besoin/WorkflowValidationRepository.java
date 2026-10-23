package s5.itu.projetgestion.repository.besoin;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.besoin.WorkflowValidation;
import java.util.List;
import java.util.Optional;

import java.util.Optional;

public interface WorkflowValidationRepository extends JpaRepository<WorkflowValidation, Integer>{
    Optional<WorkflowValidation> findFirstByOrderByOrdreAsc();

    
    List<WorkflowValidation> findByOrdre(Integer ordre);



    List<WorkflowValidation> findAllByOrderByOrdreAscIdAsc();

    boolean existsByPosteIdAndOrdreAndIdNot(Integer idPoste, Integer ordre, Integer id);
}
