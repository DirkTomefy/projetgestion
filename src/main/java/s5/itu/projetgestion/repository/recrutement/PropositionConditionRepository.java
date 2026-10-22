package s5.itu.projetgestion.repository.recrutement;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.recrutement.PropositionCondition;

import java.util.List;

public interface PropositionConditionRepository extends JpaRepository<PropositionCondition, Integer> {
    List<PropositionCondition> findByPropositionId(Integer propositionId);
}
