package s5.itu.projetgestion.repository.onboarding;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import s5.itu.projetgestion.model.onboarding.OnboardingDetail;

public interface OnboardingDetailRepository extends JpaRepository<OnboardingDetail, Integer> {

    @Query("""
        select d from OnboardingDetail d join fetch d.modele left join fetch d.posteResponsable left join fetch d.realisateur
        where d.onboarding.id = :idOnboarding order by d.ordre, d.id
        """)
    List<OnboardingDetail> findParOnboarding(@Param("idOnboarding") Integer idOnboarding);

    /** Tâches à faire dont l'utilisateur est responsable (par ses postes), onboardings en cours. */
    @Query("""
        select d from OnboardingDetail d join fetch d.onboarding o join fetch o.contrat c join fetch c.affectation a
        join fetch a.employe join fetch a.poste join fetch d.modele left join fetch d.posteResponsable
        where o.statut = 'EN_COURS' and d.statut = 'A_FAIRE' and d.posteResponsable.id in :postes
        order by d.dateEcheance, d.id
        """)
    List<OnboardingDetail> aFairePourPostes(@Param("postes") Collection<Integer> postes);

    long countByModeleId(Integer idModele);

    @Query("select d from OnboardingDetail d join d.onboarding o join o.contrat c join c.affectation a where a.employe.id = :idEmploye and d.typeTache = :type and d.statut = 'A_FAIRE' and o.statut = 'EN_COURS'")
    List<OnboardingDetail> aFaireParEmployeEtType(@Param("idEmploye") Integer idEmploye, @Param("type") String type);
}
