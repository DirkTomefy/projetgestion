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

    @org.springframework.data.jpa.repository.Query("""
        select d from Delegation d join fetch d.userDelegant join fetch d.userDelegue
        where (:idUser = 0 or d.userDelegant.id = :idUser or d.userDelegue.id = :idUser)
        order by d.dateDebut desc, d.id desc
        """)
    List<Delegation> rechercher(@org.springframework.data.repository.query.Param("idUser") Integer idUser);
}
