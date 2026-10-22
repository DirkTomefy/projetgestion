package s5.itu.projetgestion.repository.organisation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import s5.itu.projetgestion.model.organisation.Affectation;
import s5.itu.projetgestion.model.organisation.Employe;

public interface AffectationRepository extends JpaRepository<Affectation, Integer>{
    
    public Affectation findByEmploye(Employe employe);

    @Query("""
    select a from Affectation a
    where a.poste.uniteOrga.id = :idUnite
      and a.poste.etat = 'ACTIF' and a.poste.statutOccupation = 'OCCUPE'
      and (a.dateFin is null or a.dateFin >= current_date)
        """)
    List<Affectation> findOccupees(@Param("idUnite") Integer idUnite);

    @Query("""
    select a from Affectation a
    where a.poste.id = :idPoste and a.employe.id = :idEmploye
      and (a.dateFin is null or a.dateFin >= current_date)
    """)
    List<Affectation> findEnCours(@Param("idPoste") Integer idPoste,
                              @Param("idEmploye") Integer idEmploye);

}
