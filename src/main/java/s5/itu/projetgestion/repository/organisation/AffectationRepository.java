package s5.itu.projetgestion.repository.organisation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import s5.itu.projetgestion.model.organisation.Affectation;
import s5.itu.projetgestion.model.organisation.Employe;

public interface AffectationRepository extends JpaRepository<Affectation, Integer>{
    
    public Affectation findByEmploye(Employe employe);

    /** Personnel en poste (affectation en cours), filtré sur nom, prénoms ou matricule ; q = '' : tout le monde. */
    @Query("""
    select a from Affectation a
      join fetch a.employe e
      join fetch a.poste p
      join fetch p.uniteOrga u
    where (a.dateFin is null or a.dateFin >= current_date)
      and (:q = '' or lower(concat(e.nom, ' ', coalesce(e.prenoms, ''), ' ', e.matricule)) like concat('%', :q, '%'))
    order by e.nom, e.prenoms
    """)
    List<Affectation> findPersonnelEnPoste(@Param("q") String q);

    /** Affectations d'un employé, la plus récente d'abord. */
    @Query("""
    select a from Affectation a
      join fetch a.poste p
      join fetch p.uniteOrga u
    where a.employe.id = :idEmploye
    order by a.dateDebut desc, a.id desc
    """)
    List<Affectation> findHistorique(@Param("idEmploye") Integer idEmploye);

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


    /** Affectation en cours d'un employé (la plus récente), s'il en a une. */
    @Query("""
    select a from Affectation a
      join fetch a.poste p
      join fetch p.uniteOrga u
    where a.employe.id = :idEmploye
      and (a.dateFin is null or a.dateFin >= current_date)
    order by a.dateDebut desc, a.id desc
    """)
    List<Affectation> findEnCoursParEmploye(@Param("idEmploye") Integer idEmploye);

    /** Affectations en cours d'un poste. */
    @Query("""
    select a from Affectation a join fetch a.employe e
    where a.poste.id = :idPoste and (a.dateFin is null or a.dateFin >= current_date)
    """)
    List<Affectation> findEnCoursParPoste(@Param("idPoste") Integer idPoste);

    /** Affectations terminées dont le poste est encore marqué OCCUPE sans autre occupant (à libérer). */
    @Query("""
    select a from Affectation a join fetch a.poste p
    where a.dateFin is not null and a.dateFin < current_date and p.statutOccupation = 'OCCUPE'
      and not exists (select 1 from Affectation b where b.poste = p and (b.dateFin is null or b.dateFin >= current_date))
    """)
    List<Affectation> findTermineesAvecPosteOccupe();

    /** Tout le personnel (y compris parti), avec sa dernière affectation. */
    @Query("""
    select a from Affectation a join fetch a.employe e join fetch a.poste p join fetch p.uniteOrga u
    where a.id = (select max(b.id) from Affectation b where b.employe = e)
      and (:q = '' or lower(concat(e.nom, ' ', coalesce(e.prenoms, ''), ' ', e.matricule)) like concat('%', :q, '%'))
    order by e.nom, e.prenoms
    """)
    List<Affectation> findDernieresAffectations(@Param("q") String q);
}
