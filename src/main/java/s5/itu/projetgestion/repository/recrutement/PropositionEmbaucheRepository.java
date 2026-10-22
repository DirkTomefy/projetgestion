package s5.itu.projetgestion.repository.recrutement;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.recrutement.PropositionEmbauche;

import java.util.List;

public interface PropositionEmbaucheRepository extends JpaRepository<PropositionEmbauche, Integer> {
    List<PropositionEmbauche> findByStatutActuel(String statutActuel);
}
