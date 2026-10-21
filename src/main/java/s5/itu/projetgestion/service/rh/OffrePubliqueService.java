package s5.itu.projetgestion.service.rh;

import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import s5.itu.projetgestion.dto.rh.OffrePubliqueDto;
import s5.itu.projetgestion.dto.rh.TypeInformationDto;
import s5.itu.projetgestion.dto.rh.UniteFiltreDto;
import s5.itu.projetgestion.err.ProjetgestionException;
import s5.itu.projetgestion.model.rh.TypeInformation;
import s5.itu.projetgestion.repository.rh.TypeInformationRepository;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OffrePubliqueService {

    private static final String OFFRE_VISIBLE = """
            o.etat = 'PUBLIEE'
              AND (o.date_publication IS NULL OR o.date_publication <= CURRENT_DATE)
              AND (o.date_limite_candidature IS NULL OR o.date_limite_candidature >= CURRENT_DATE)
            """;

    private static final String SELECT_OFFRES = """
            SELECT o.id, o.ref, o.date_publication, o.date_limite_candidature,
                   tc.libelle  AS type_contrat,
                   u.libelle   AS unite,
                   dir.libelle AS direction,
                   string_agg(DISTINCT p.libelle, ', ' ORDER BY p.libelle) AS postes,
                   count(bp.id) AS nb_postes,
                   (o.id_document IS NOT NULL) AS fiche_disponible
            FROM offre_emploi o
            JOIN besoin b             ON b.id = o.id_besoin
            JOIN unite_orga u         ON u.id = b.id_unite_orga
            LEFT JOIN type_contrat tc ON tc.id = o.id_type_contrat
            LEFT JOIN besoin_poste bp ON bp.id_besoin = b.id AND bp.etat NOT IN ('POURVU', 'ANNULE')
            LEFT JOIN poste p         ON p.id = bp.id_poste
            LEFT JOIN LATERAL (
                WITH RECURSIVE ancetres AS (
                    SELECT id, type, libelle, id_unite_mere, 0 AS niveau
                    FROM unite_orga WHERE id = b.id_unite_orga
                    UNION ALL
                    SELECT m.id, m.type, m.libelle, m.id_unite_mere, a.niveau + 1
                    FROM unite_orga m JOIN ancetres a ON m.id = a.id_unite_mere
                )
                SELECT libelle FROM ancetres WHERE type = 'DIRECTION' ORDER BY niveau LIMIT 1
            ) dir ON TRUE
            WHERE
            """ + OFFRE_VISIBLE;

    private static final String FILTRE_UNITE = """
              AND b.id_unite_orga IN (
                  WITH RECURSIVE sous_unites AS (
                      SELECT id FROM unite_orga WHERE id = ?
                      UNION ALL
                      SELECT u2.id FROM unite_orga u2 JOIN sous_unites s ON u2.id_unite_mere = s.id
                  )
                  SELECT id FROM sous_unites)
            """;

    private static final String FILTRE_ID = """
              AND o.id = ?
            """;

    private static final String GROUPE_ET_TRI = """
            GROUP BY o.id, tc.libelle, u.libelle, dir.libelle
            ORDER BY o.date_limite_candidature NULLS LAST, o.id
            """;

    private static final String SELECT_UNITES_FILTRE = """
            WITH RECURSIVE arbre AS (
                SELECT id, type, libelle, 0 AS profondeur, ARRAY[libelle::text] AS chemin
                FROM unite_orga WHERE id_unite_mere IS NULL
                UNION ALL
                SELECT u.id, u.type, u.libelle, a.profondeur + 1, a.chemin || u.libelle::text
                FROM unite_orga u JOIN arbre a ON u.id_unite_mere = a.id
            )
            SELECT id, type, libelle, profondeur FROM arbre
            WHERE type IN ('DIRECTION', 'DEPARTEMENT')
            ORDER BY chemin
            """;

    private static final String SELECT_OFFRE_OUVERTE = """
            SELECT count(*) FROM offre_emploi o
            WHERE o.id = ? AND
            """ + OFFRE_VISIBLE;

    private static final String SELECT_DOCUMENT_OFFRE = """
            SELECT o.id_document FROM offre_emploi o
            WHERE o.id = ? AND o.id_document IS NOT NULL AND
            """ + OFFRE_VISIBLE;

    private static final Map<String, String> LIBELLES_TYPES = Map.of(
            TypeInformation.EXPERIENCE_PRO, "Expérience professionnelle",
            TypeInformation.EDUCATION, "Formation / diplôme",
            TypeInformation.QUALITE, "Qualité / compétence");

    private static final RowMapper<OffrePubliqueDto> MAPPER = (rs, i) -> {
        Date pub = rs.getDate("date_publication");
        Date lim = rs.getDate("date_limite_candidature");
        return new OffrePubliqueDto(
                rs.getInt("id"),
                rs.getString("ref"),
                pub != null ? pub.toLocalDate() : null,
                lim != null ? lim.toLocalDate() : null,
                rs.getString("type_contrat"),
                rs.getString("unite"),
                rs.getString("direction"),
                rs.getString("postes"),
                rs.getInt("nb_postes"),
                rs.getBoolean("fiche_disponible"));
    };

    private final JdbcTemplate jdbc;
    private final TypeInformationRepository typeInformationRepo;
    private final FileStorageService fileStorageService;

    public OffrePubliqueService(JdbcTemplate jdbc, TypeInformationRepository typeInformationRepo,
                                FileStorageService fileStorageService) {
        this.jdbc = jdbc;
        this.typeInformationRepo = typeInformationRepo;
        this.fileStorageService = fileStorageService;
    }

    public List<OffrePubliqueDto> listerOffres(Integer idUnite) {
        StringBuilder sql = new StringBuilder(SELECT_OFFRES);
        List<Object> params = new ArrayList<>();
        if (idUnite != null) {
            sql.append(FILTRE_UNITE);
            params.add(idUnite);
        }
        sql.append(GROUPE_ET_TRI);
        return jdbc.query(sql.toString(), MAPPER, params.toArray());
    }

    public OffrePubliqueDto trouverOffre(Integer idOffre) {
        return jdbc.query(SELECT_OFFRES + FILTRE_ID + GROUPE_ET_TRI, MAPPER, idOffre).stream().findFirst()
                .orElseThrow(() -> new OffreIntrouvableException(
                        "Cette offre n'existe pas ou n'est plus ouverte aux candidatures"));
    }

    public boolean estOuverte(Integer idOffre) {
        Integer nb = jdbc.queryForObject(SELECT_OFFRE_OUVERTE, Integer.class, idOffre);
        return nb != null && nb > 0;
    }

    public Resource ficheOffre(Integer idOffre) {
        Integer idDocument = jdbc.queryForList(SELECT_DOCUMENT_OFFRE, Integer.class, idOffre).stream()
                .findFirst()
                .orElseThrow(() -> new OffreIntrouvableException("Fiche de l'offre indisponible"));
        try {
            return fileStorageService.loadAsResource(idDocument);
        } catch (IllegalArgumentException e) {
            throw new OffreIntrouvableException("Fiche de l'offre indisponible");
        }
    }

    public List<UniteFiltreDto> listerUnitesFiltre() {
        return jdbc.query(SELECT_UNITES_FILTRE, (rs, i) -> new UniteFiltreDto(
                rs.getInt("id"), rs.getString("type"), rs.getString("libelle"), rs.getInt("profondeur")));
    }

    public List<TypeInformationDto> listerTypesInformation() {
        return typeInformationRepo.findAllByOrderByIdAsc().stream()
                .map(t -> new TypeInformationDto(t.getLibelle(),
                        LIBELLES_TYPES.getOrDefault(t.getLibelle(), t.getLibelle())))
                .toList();
    }

    public static class OffreIntrouvableException extends ProjetgestionException {
        public OffreIntrouvableException(String message) {
            super(message);
        }
    }
}
