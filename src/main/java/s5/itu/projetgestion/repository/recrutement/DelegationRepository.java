package s5.itu.projetgestion.repository.recrutement;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.auth.User;
import s5.itu.projetgestion.model.recrutement.Delegation;

import java.time.LocalDate;
import java.util.List;

public interface DelegationRepository extends JpaRepository<Delegation, Integer> {
    List<Delegation> findByUserDelegantAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(
            User userDelegant, LocalDate dateFin, LocalDate dateDebut);

    List<Delegation> findByUserDelegueAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(
            User userDelegue, LocalDate date, LocalDate sameDate);
}
