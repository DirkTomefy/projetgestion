package s5.itu.projetgestion.repository.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import s5.itu.projetgestion.model.finance.GrilleSalariale;

public interface GrilleSalarialeRepository extends JpaRepository<GrilleSalariale, Integer> {

    /** Ligne de grille d'un poste (catégorie de son métier × son échelon) en vigueur à une date. */
    interface FourchetteRow {
        Integer getIdPoste();
        String getCategorie();
        String getEchelon();
        BigDecimal getMontantMin();
        BigDecimal getMontantMax();
        Integer getIdGrille();
        LocalDate getDateEffectif();
    }

    @Query(value = """
            SELECT p.id AS "idPoste", c.libelle AS "categorie", e.libelle AS "echelon",
                   d.montant_min AS "montantMin", d.montant_max AS "montantMax",
                   g.id AS "idGrille", g.date_effectif AS "dateEffectif"
            FROM poste p
            JOIN metier m            ON m.id = p.id_metier
            JOIN categorie_metier c  ON c.id = m.id_categorie
            JOIN echelon_metier e    ON e.id = p.id_echelon
            JOIN grille_salariale_details d ON d.id_categorie = m.id_categorie AND d.id_echelon = p.id_echelon
            JOIN grille_salariale g  ON g.id = d.id_grille
            WHERE p.id = :idPoste
              AND g.statut = 'VALIDEE'
              AND g.date_effectif <= :jour
              AND (g.date_fin IS NULL OR g.date_fin >= :jour)
            ORDER BY g.date_effectif DESC, g.id DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<FourchetteRow> fourchette(@Param("idPoste") Integer idPoste, @Param("jour") LocalDate jour);

    List<GrilleSalariale> findAllByOrderByDateEffectifDesc();

    long countByStatut(String statut);

    /** Grille en vigueur à une date. */
    @Query("""
            select g from GrilleSalariale g
            where g.statut = 'VALIDEE' and g.dateEffectif <= :jour and (g.dateFin is null or g.dateFin >= :jour)
            order by g.dateEffectif desc, g.id desc
            """)
    List<GrilleSalariale> enVigueur(@Param("jour") LocalDate jour);
}
