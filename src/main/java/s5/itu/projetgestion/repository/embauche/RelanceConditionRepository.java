package s5.itu.projetgestion.repository.embauche;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.embauche.RelanceCondition;

import java.util.List;

public interface RelanceConditionRepository extends JpaRepository<RelanceCondition, Integer> {
    List<RelanceCondition> findByPropositionConditionId(Integer propositionConditionId);
}
