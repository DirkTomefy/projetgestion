package s5.itu.projetgestion.repository.besoin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import s5.itu.projetgestion.model.besoin.Besoin;
import s5.itu.projetgestion.model.besoin.Validation;

public interface ValidationRepository extends JpaRepository<Validation, Integer>{
    
    Optional<Validation> findFirstByBesoinOrderByDateDesc(Besoin besoin);

    /** Dernière étape franchie (décision VALIDE) ; la ligne « Demande soumise » (EN_ATTENTE) ne compte pas. */
    Optional<Validation> findFirstByBesoinAndDecisionOrderByDateDescIdDesc(Besoin besoin, String decision);


    @org.springframework.data.jpa.repository.Query("""
        select v from Validation v join fetch v.user join fetch v.workflowValidation w join fetch w.poste
        where v.besoin.id = :idBesoin order by v.date, v.id
        """)
    java.util.List<Validation> findParBesoin(@org.springframework.data.repository.query.Param("idBesoin") Integer idBesoin);
}
