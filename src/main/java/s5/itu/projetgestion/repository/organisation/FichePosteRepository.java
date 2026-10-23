package s5.itu.projetgestion.repository.organisation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import s5.itu.projetgestion.model.organisation.FichePoste;

import java.util.List;
import java.util.Optional;

public interface FichePosteRepository extends JpaRepository<FichePoste, Integer> {

    Optional<FichePoste> findByIdMetier(Integer idMetier);

    Optional<FichePoste> findByIdPoste(Integer idPoste);

    /** Ligne du catalogue : un métier, sa fiche modèle éventuelle et le nombre de postes qui l'utilisent. */
    interface LigneCatalogue {
        Integer getIdMetier();
        String getMetier();
        String getCategorie();
        Integer getIdFiche();
        Integer getVersion();
        java.time.LocalDateTime getDateMaj();
        Long getNbPostes();
        Long getNbPostesPropres();
    }

    @Query(value = """
        SELECT m.id AS idMetier, m.libelle AS metier, c.libelle AS categorie,
               f.id AS idFiche, f.version AS version, f.date_maj AS dateMaj,
               (SELECT count(*) FROM poste p WHERE p.id_metier = m.id) AS nbPostes,
               (SELECT count(*) FROM fiche_poste fp JOIN poste p ON p.id = fp.id_poste WHERE p.id_metier = m.id) AS nbPostesPropres
        FROM metier m
        LEFT JOIN categorie_metier c ON c.id = m.id_categorie
        LEFT JOIN fiche_poste f ON f.id_metier = m.id
        ORDER BY m.libelle
        """, nativeQuery = true)
    List<LigneCatalogue> catalogue();

    /** Fiche applicable à un poste : la sienne si elle existe, sinon celle de son métier. */
    @Query(value = """
        SELECT f.* FROM fiche_poste f
        WHERE f.id_poste = :idPoste
           OR f.id_metier = (SELECT id_metier FROM poste WHERE id = :idPoste)
        ORDER BY (f.id_poste IS NULL), f.id
        LIMIT 1
        """, nativeQuery = true)
    Optional<FichePoste> applicable(@Param("idPoste") Integer idPoste);

    @Query(value = "SELECT count(*) FROM offre_emploi WHERE id_fiche_poste = :id", nativeQuery = true)
    long offresLiees(@Param("id") Integer id);
}
