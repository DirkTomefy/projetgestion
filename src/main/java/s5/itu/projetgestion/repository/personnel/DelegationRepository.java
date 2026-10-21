package s5.itu.projetgestion.repository.personnel;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.User;
import s5.itu.projetgestion.model.personnel.Delegation;

import java.time.LocalDate;
import java.util.List;

public interface DelegationRepository extends JpaRepository<Delegation, Integer> {
    List<Delegation> findByUserDelegantAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(
            User userDelegant, LocalDate date, LocalDate sameDate);
}