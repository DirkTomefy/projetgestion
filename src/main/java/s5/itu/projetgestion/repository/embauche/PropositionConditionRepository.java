package s5.itu.projetgestion.repository.embauche;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.embauche.PropositionCondition;

import java.util.List;

public interface PropositionConditionRepository extends JpaRepository<PropositionCondition, Integer> {

    /** Utilisée aussi par l'embauche (P3) pour recopier les conditions acceptées. */
    List<PropositionCondition> findByPropositionId(Integer idProposition);

    List<PropositionCondition> findByPropositionIdOrderByPointConditionIdAsc(Integer idProposition);
}
