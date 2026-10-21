package s5.itu.projetgestion.repository.personnel;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.personnel.Candidat;

public interface CandidatRepository extends JpaRepository<Candidat, Integer> {
}