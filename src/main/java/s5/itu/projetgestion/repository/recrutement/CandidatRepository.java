package s5.itu.projetgestion.repository.recrutement;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.recrutement.Candidat;

import java.util.Optional;

public interface CandidatRepository extends JpaRepository<Candidat, Integer> {
    Optional<Candidat> findFirstByEmailIgnoreCase(String email);
}
