package s5.itu.projetgestion.repository.recrutement;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.recrutement.RelanceCondition;

import java.util.List;

public interface RelanceConditionRepository extends JpaRepository<RelanceCondition, Integer> {
    List<RelanceCondition> findByPropositionConditionId(Integer propositionConditionId);
}
