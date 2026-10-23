package s5.itu.projetgestion.repository.onboarding;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import s5.itu.projetgestion.model.onboarding.Onboarding;

public interface OnboardingRepository extends JpaRepository<Onboarding, Integer> {

    boolean existsByContratId(Integer idContrat);

    Optional<Onboarding> findByContratId(Integer idContrat);

    long countByWorkflowId(Integer idWorkflow);

    @Query("""
        select o from Onboarding o join fetch o.contrat c join fetch c.affectation a join fetch a.employe join fetch a.poste p
        join fetch p.uniteOrga join fetch o.workflow
        where (:statut = '' or o.statut = :statut)
        order by o.dateDebut desc, o.id desc
        """)
    List<Onboarding> rechercher(@Param("statut") String statut);

    @Query("""
        select o from Onboarding o join fetch o.contrat c join fetch c.affectation a join fetch a.employe
        where a.employe.id = :idEmploye order by o.id desc
        """)
    List<Onboarding> findParEmploye(@Param("idEmploye") Integer idEmploye);
}
