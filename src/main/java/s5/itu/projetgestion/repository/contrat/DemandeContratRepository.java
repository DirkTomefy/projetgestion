package s5.itu.projetgestion.repository.contrat;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import s5.itu.projetgestion.model.contrat.DemandeContrat;

public interface DemandeContratRepository extends JpaRepository<DemandeContrat, Integer> {

    boolean existsByContratIdAndEtatIn(Integer idContrat, Collection<String> etats);

    long countByReferenceStartingWith(String prefix);

    boolean existsByReference(String reference);

    long countByEtat(String etat);

    @Query("""
            SELECT d FROM DemandeContrat d JOIN FETCH d.contrat c JOIN FETCH c.affectation a
            JOIN FETCH a.employe JOIN FETCH a.poste p JOIN FETCH p.uniteOrga JOIN FETCH c.type
            WHERE d.etat IN :etats
            ORDER BY d.dateDemande DESC, d.id DESC
            """)
    List<DemandeContrat> parEtats(@Param("etats") Collection<String> etats);

    @Query("""
            SELECT d FROM DemandeContrat d JOIN FETCH d.contrat c JOIN FETCH c.affectation a
            JOIN FETCH a.employe JOIN FETCH a.poste p JOIN FETCH p.uniteOrga JOIN FETCH c.type
            WHERE (:idEmploye IS NULL OR a.employe.id = :idEmploye)
            ORDER BY d.dateDemande DESC, d.id DESC
            """)
    List<DemandeContrat> toutes(@Param("idEmploye") Integer idEmploye);

    /** Demandes faites par l'utilisateur ou concernant l'employé donné. */
    @Query("""
            SELECT d FROM DemandeContrat d JOIN FETCH d.contrat c JOIN FETCH c.affectation a
            JOIN FETCH a.employe JOIN FETCH a.poste p JOIN FETCH p.uniteOrga JOIN FETCH c.type
            WHERE d.idDemandeur = :idUser OR (:idEmploye IS NOT NULL AND a.employe.id = :idEmploye)
            ORDER BY d.dateDemande DESC, d.id DESC
            """)
    List<DemandeContrat> mesDemandes(@Param("idUser") Integer idUser, @Param("idEmploye") Integer idEmploye);
}
