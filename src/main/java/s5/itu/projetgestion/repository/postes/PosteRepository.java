package s5.itu.projetgestion.repository.postes;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import s5.itu.projetgestion.model.postes.Poste;

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
}