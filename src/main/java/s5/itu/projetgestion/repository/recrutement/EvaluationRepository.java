package s5.itu.projetgestion.repository.recrutement;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.recrutement.Evaluation;

import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Integer> {

    List<Evaluation> findByTraitement_Candidature_IdOrderByNiveau_OrdreAsc(Integer idCandidature);

    boolean existsByNiveau_Id(Integer idNiveau);
}
