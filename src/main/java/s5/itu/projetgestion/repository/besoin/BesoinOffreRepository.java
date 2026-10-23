package s5.itu.projetgestion.repository.besoin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import s5.itu.projetgestion.model.besoin.Besoin;

/** Besoins vus depuis les offres d'emploi (P4) : poste, unité, fourchette salariale de la grille en vigueur. */
public interface BesoinOffreRepository extends Repository<Besoin, Integer> {

    interface BesoinOffreRow {
        Integer getId();
        String getReference();
        String getTypeBesoin();
        Integer getEffectif();
        LocalDate getDateSouhaitee();
        String getEtatActuel();
        String getPrecisionMotif();
        String getMotif();
        String getUnite();
        String getUniteType();
        String getPoste();
        String getMetier();
        String getCategorie();
        String getEchelon();
        BigDecimal getMontantMin();
        BigDecimal getMontantMax();
    }

    String SELECT = """
        SELECT b.id AS "id", b.reference AS "reference", b.type_besoin AS "typeBesoin", b.effectif AS "effectif",
               b.date_souhaitee AS "dateSouhaitee", b.etat_actuel AS "etatActuel", b.precision_motif AS "precisionMotif",
               m.libelle AS "motif", u.libelle AS "unite", u.type AS "uniteType",
               p.libelle AS "poste", me.libelle AS "metier", c.libelle AS "categorie", e.libelle AS "echelon",
               gd.montant_min AS "montantMin", gd.montant_max AS "montantMax"
        FROM besoin b
        JOIN motif m      ON m.id = b.id_motif
        JOIN unite_orga u ON u.id = b.id_unite_orga
        LEFT JOIN LATERAL (SELECT bp.id_poste FROM besoin_poste bp
                           WHERE bp.id_besoin = b.id ORDER BY bp.id LIMIT 1) bp ON TRUE
        LEFT JOIN poste p            ON p.id = bp.id_poste
        LEFT JOIN metier me          ON me.id = p.id_metier
        LEFT JOIN categorie_metier c ON c.id = me.id_categorie
        LEFT JOIN echelon_metier e   ON e.id = p.id_echelon
        LEFT JOIN LATERAL (
            SELECT d.montant_min, d.montant_max
            FROM grille_salariale g
            JOIN grille_salariale_details d ON d.id_grille = g.id
            WHERE d.id_categorie = me.id_categorie AND d.id_echelon = p.id_echelon
              AND g.statut = 'VALIDEE' AND g.date_effectif <= CURRENT_DATE
              AND (g.date_fin IS NULL OR g.date_fin >= CURRENT_DATE)
            ORDER BY g.date_effectif DESC LIMIT 1) gd ON TRUE
        """;

    @Query(value = SELECT + " WHERE b.id = :id", nativeQuery = true)
    Optional<BesoinOffreRow> findInfo(@Param("id") Integer id);

    @Query(value = SELECT + " WHERE b.etat_actuel IN ('RECRUTEMENT_OUVERT', 'OUVERT') ORDER BY b.id DESC", nativeQuery = true)
    List<BesoinOffreRow> findRecrutementOuvert();
}
