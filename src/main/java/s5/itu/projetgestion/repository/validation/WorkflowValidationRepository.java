package s5.itu.projetgestion.repository.validation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import s5.itu.projetgestion.model.validation.WorkflowValidation;

public interface WorkflowValidationRepository extends JpaRepository<WorkflowValidation, Integer>{
    Optional<WorkflowValidation> findFirstByOrderByOrdreAsc();

    
    List<WorkflowValidation> findByOrdre(Integer ordre);


}
