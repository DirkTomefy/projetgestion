package s5.itu.projetgestion.repository.recrutement;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.recrutement.HistoCandidature;

public interface HistoCandidatureRepository extends JpaRepository<HistoCandidature, Integer> {
}
