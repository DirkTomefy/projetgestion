package s5.itu.projetgestion.service.recrutement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import s5.itu.projetgestion.dto.recrutement.BesoinOffreInfo;

import java.sql.Date;
import java.util.List;
import java.util.Optional;


@Service
public class BesoinOffreLookupService {

    public static final List<String> ETATS_RECRUTEMENT = List.of("RECRUTEMENT_OUVERT", "OUVERT");

    private static final String SELECT = """
        SELECT b.id, b.reference, b.type_besoin, b.effectif, b.date_souhaitee, b.etat_actuel,
               b.precision_motif, m.libelle AS motif, u.libelle AS unite, u.type AS unite_type,
               p.libelle AS poste, me.libelle AS metier, c.libelle AS categorie, e.libelle AS echelon,
               gd.montant_min, gd.montant_max
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
              AND g.date_effectif <= CURRENT_DATE
              AND (g.date_fin IS NULL OR g.date_fin >= CURRENT_DATE)
            ORDER BY g.date_effectif DESC LIMIT 1) gd ON TRUE
        """;

    private static final RowMapper<BesoinOffreInfo> MAPPER = (rs, i) -> {
        Date ds = rs.getDate("date_souhaitee");
        return new BesoinOffreInfo(
                rs.getInt("id"),
                rs.getString("reference"),
                rs.getString("type_besoin"),
                rs.getInt("effectif"),
                ds != null ? ds.toLocalDate() : null,
                rs.getString("etat_actuel"),
                rs.getString("precision_motif"),
                rs.getString("motif"),
                rs.getString("unite"),
                rs.getString("unite_type"),
                rs.getString("poste"),
                rs.getString("metier"),
                rs.getString("categorie"),
                rs.getString("echelon"),
                rs.getBigDecimal("montant_min"),
                rs.getBigDecimal("montant_max"));
    };

    private final JdbcTemplate jdbc;

    public BesoinOffreLookupService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<BesoinOffreInfo> findById(Integer id) {
        if (id == null) return Optional.empty();
        return jdbc.query(SELECT + " WHERE b.id = ?", MAPPER, id).stream().findFirst();
    }

    public List<BesoinOffreInfo> findRecrutementOuvert() {
        return jdbc.query(SELECT + " WHERE b.etat_actuel IN ('RECRUTEMENT_OUVERT', 'OUVERT') ORDER BY b.id DESC",
                MAPPER);
    }
}
