package s5.itu.projetgestion.service.finance;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Interface commune (P3, P4) : fourchette salariale d'un poste à une date.
 * La grille retenue est celle en vigueur à cette date (date_effectif ≤ date ≤ date_fin) ;
 * la ligne est celle de la catégorie du métier du poste et de l'échelon du poste.
 */
@Service
public class GrilleSalarialeService {

    private static final String SQL = """
            SELECT p.id AS id_poste, c.libelle AS categorie, e.libelle AS echelon,
                   d.montant_min, d.montant_max, g.id AS id_grille, g.date_effectif
            FROM poste p
            JOIN metier m            ON m.id = p.id_metier
            JOIN categorie_metier c  ON c.id = m.id_categorie
            JOIN echelon_metier e    ON e.id = p.id_echelon
            JOIN grille_salariale_details d ON d.id_categorie = m.id_categorie AND d.id_echelon = p.id_echelon
            JOIN grille_salariale g  ON g.id = d.id_grille
            WHERE p.id = ?
              AND g.date_effectif <= ?
              AND (g.date_fin IS NULL OR g.date_fin >= ?)
            ORDER BY g.date_effectif DESC, g.id DESC
            LIMIT 1
            """;

    private final JdbcTemplate jdbc;

    public GrilleSalarialeService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Vide si le poste n'existe pas ou si aucune grille n'est en vigueur à cette date. */
    public Optional<FourchetteSalariale> fourchette(Integer idPoste, LocalDate date) {
        if (idPoste == null) return Optional.empty();
        Date jour = Date.valueOf(date != null ? date : LocalDate.now());
        return jdbc.query(SQL, (rs, i) -> new FourchetteSalariale(
                        rs.getInt("id_poste"),
                        rs.getString("categorie"),
                        rs.getString("echelon"),
                        rs.getBigDecimal("montant_min"),
                        rs.getBigDecimal("montant_max"),
                        rs.getInt("id_grille"),
                        rs.getDate("date_effectif").toLocalDate()),
                idPoste, jour, jour).stream().findFirst();
    }
}
