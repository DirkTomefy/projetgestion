package s5.itu.projetgestion.repository.embauche;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.embauche.PropositionCondition;

import java.util.List;

public interface PropositionConditionRepository extends JpaRepository<PropositionCondition, Integer> {
    List<PropositionCondition> findByPropositionId(Integer propositionId);
}
