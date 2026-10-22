package s5.itu.projetgestion.repository.embauche;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.embauche.PointCondition;

import java.util.Optional;

public interface PointConditionRepository extends JpaRepository<PointCondition, Integer> {
    Optional<PointCondition> findByLibelle(String libelle);
}
