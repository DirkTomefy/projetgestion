package s5.itu.projetgestion.repository.embauche;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.embauche.RelanceCondition;

import java.util.List;
import java.util.Optional;

public interface RelanceConditionRepository extends JpaRepository<RelanceCondition, Integer> {

    List<RelanceCondition> findByPropositionCondition_Proposition_IdOrderByDateRelanceAscIdAsc(Integer idProposition);

    Optional<RelanceCondition> findFirstByPropositionCondition_IdAndStatutOrderByIdDesc(Integer idCondition, String statut);
}
