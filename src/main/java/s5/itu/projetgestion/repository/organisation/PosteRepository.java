package s5.itu.projetgestion.repository.organisation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import s5.itu.projetgestion.model.organisation.Poste;

public interface PosteRepository extends JpaRepository<Poste, Integer> {

    List<Poste> findByEtat(String etat);
    List<Poste> findByEtatAndUniteOrgaId(String etat, Integer idUnite);
    boolean existsByCode(String code);

    // JOIN FETCH : charge les relations LAZY en une seule requête
    @Query("""
            SELECT p FROM Poste p
            JOIN FETCH p.metier
            JOIN FETCH p.echelon
            JOIN FETCH p.uniteOrga
            LEFT JOIN FETCH p.superieurDirect
            WHERE p.etat = :etat
            ORDER BY p.code
            """)
    List<Poste> findByEtatAvecRelations(@Param("etat") String etat);

    List<Poste> findByEtatAndStatutOccupationAndUniteOrgaId(String etat, String statut, Integer idUnite);

    long countByUniteOrgaId(Integer idUnite);

    long countBySuperieurDirectIdAndEtatNot(Integer idPoste, String etat);

    /** Recherche multicritère ('' ou null = pas de filtre). */
    @Query("""
            SELECT p FROM Poste p
            JOIN FETCH p.metier m
            JOIN FETCH m.categorie
            JOIN FETCH p.echelon
            JOIN FETCH p.uniteOrga u
            LEFT JOIN FETCH p.superieurDirect
            WHERE (:etat = '' OR p.etat = :etat)
              AND (:occupation = '' OR p.statutOccupation = :occupation)
              AND (:idUnite = 0 OR u.id = :idUnite)
              AND (:q = '' OR lower(concat(p.code, ' ', p.libelle, ' ', m.libelle)) LIKE concat('%', :q, '%'))
            ORDER BY u.libelle, p.libelle
            """)
    List<Poste> rechercher(@Param("etat") String etat, @Param("occupation") String occupation,
                           @Param("idUnite") Integer idUnite, @Param("q") String q);

    @Query("""
            SELECT p FROM Poste p
            JOIN FETCH p.metier m JOIN FETCH m.categorie JOIN FETCH p.echelon JOIN FETCH p.uniteOrga
            LEFT JOIN FETCH p.superieurDirect
            WHERE p.id = :id
            """)
    java.util.Optional<Poste> findAvecRelations(@Param("id") Integer id);

    List<Poste> findBySuperieurDirectIdAndEtatNot(Integer idPoste, String etat);

    /** Postes proposés hors besoin, en attente de la décision du DG. */
    @Query("""
            SELECT COUNT(p) FROM Poste p WHERE p.etat = 'PROPOSE'
            AND NOT EXISTS (SELECT 1 FROM BesoinPoste bp WHERE bp.poste = p AND bp.etat = 'EN_ATTENTE')
            """)
    long countProposesAValider();
}
