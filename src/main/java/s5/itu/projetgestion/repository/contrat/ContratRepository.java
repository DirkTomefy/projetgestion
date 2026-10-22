package s5.itu.projetgestion.repository.contrat;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.contrat.Contrat;

public interface ContratRepository extends JpaRepository<Contrat, Integer> {
    Optional<Contrat> findByReference(String reference);
    List<Contrat> findByAffectationEmployeIdOrderByDateDebutDesc(Integer idEmploye);
    List<Contrat> findByStatutActuelAndDateFinPrevueBetween(String statut, LocalDate debut, LocalDate fin);
    boolean existsByReference(String reference);
    long countByReferenceStartingWith(String prefix);
    // Contrats arrivant à échéance
    List<Contrat> findByStatutActuelAndDateFinPrevueBetweenOrderByDateFinPrevueAsc(
        String statut, LocalDate debut, LocalDate fin);

        List<Contrat> findByAffectationIdAndStatutActuelOrderByDateDebutDesc(Integer idAffectation, String statut);
}
