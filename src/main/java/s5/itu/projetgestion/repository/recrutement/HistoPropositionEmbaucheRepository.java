package s5.itu.projetgestion.repository.recrutement;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.recrutement.HistoPropositionEmbauche;

import java.util.List;

public interface HistoPropositionEmbaucheRepository extends JpaRepository<HistoPropositionEmbauche, Integer> {
    List<HistoPropositionEmbauche> findByPropositionEmbaucheIdOrderByDateDesc(Integer propositionId);
}
