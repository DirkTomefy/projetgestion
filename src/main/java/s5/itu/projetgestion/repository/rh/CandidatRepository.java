package s5.itu.projetgestion.repository.rh;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.rh.Candidat;

import java.util.Optional;

public interface CandidatRepository extends JpaRepository<Candidat, Integer> {

    Optional<Candidat> findFirstByEmailIgnoreCase(String email);
}
