package s5.itu.projetgestion.repository.rh;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.rh.besoin.WorkflowValidation;

public interface WorkflowValidationRepository extends JpaRepository<WorkflowValidation, Integer> {
    Optional<WorkflowValidation> findFirstByOrderByOrdreAsc();            // 1re étape du circuit
}


