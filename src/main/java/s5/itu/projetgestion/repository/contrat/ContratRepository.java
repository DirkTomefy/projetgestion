package s5.itu.projetgestion.repository.contrat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.contrat.Contrat;

public interface ContratRepository extends JpaRepository<Contrat, Integer> {

    boolean existsByPropositionId(Integer idProposition);
    Optional<Contrat> findByReference(String reference);
    List<Contrat> findByAffectationEmployeIdOrderByDateDebutDesc(Integer idEmploye);
    List<Contrat> findByStatutActuelAndDateFinPrevueBetween(String statut, LocalDate debut, LocalDate fin);
    boolean existsByReference(String reference);

    @org.springframework.data.jpa.repository.Query("""
            SELECT c FROM Contrat c JOIN FETCH c.affectation a JOIN FETCH a.employe JOIN FETCH a.poste p JOIN FETCH p.uniteOrga JOIN FETCH c.type
            WHERE c.statutActuel = 'SIGNE' ORDER BY a.employe.nom, a.employe.prenoms
            """)
    List<Contrat> findSignes();
    long countByReferenceStartingWith(String prefix);
    // Contrats arrivant à échéance
    List<Contrat> findByStatutActuelAndDateFinPrevueBetweenOrderByDateFinPrevueAsc(
        String statut, LocalDate debut, LocalDate fin);

        List<Contrat> findByAffectationIdAndStatutActuelOrderByDateDebutDesc(Integer idAffectation, String statut);
}
