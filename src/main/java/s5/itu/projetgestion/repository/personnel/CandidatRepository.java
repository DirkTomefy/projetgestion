package s5.itu.projetgestion.repository.personnel;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.personnel.Candidat;

import java.util.Optional;

public interface CandidatRepository extends JpaRepository<Candidat, Integer> {
    Optional<Candidat> findFirstByEmailIgnoreCase(String email);
}
