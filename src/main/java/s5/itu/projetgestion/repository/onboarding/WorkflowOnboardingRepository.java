package s5.itu.projetgestion.repository.onboarding;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.onboarding.WorkflowOnboarding;

public interface WorkflowOnboardingRepository extends JpaRepository<WorkflowOnboarding, Integer> {
    List<WorkflowOnboarding> findByStatutOrderByIdAsc(String statut);
    List<WorkflowOnboarding> findAllByOrderByIdAsc();
}
