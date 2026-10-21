package s5.itu.projetgestion.repository.recrutement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import s5.itu.projetgestion.model.recrutement.OffreEmploi;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OffreEmploiRepository extends JpaRepository<OffreEmploi, Integer> {


    boolean existsByRef(String ref);

    boolean existsByIdBesoinAndEtatIn(Integer idBesoin, Collection<String> etats);

    List<OffreEmploi> findAllByOrderByIdDesc();

    @Query(nativeQuery = true, value = """
            SELECT o.id AS id, o.ref AS ref, o.etat AS etat,
                   o.date_limite_candidature AS dateLimiteCandidature, u.libelle AS unite,
                   (SELECT string_agg(DISTINCT p.libelle, ', ' ORDER BY p.libelle)
                      FROM besoin_poste bp JOIN poste p ON p.id = bp.id_poste
                     WHERE bp.id_besoin = o.id_besoin) AS postes,
                   (SELECT count(*) FROM candidatures c WHERE c.id_offre_emploi = o.id) AS nbCandidatures,
                   (SELECT count(*) FROM candidatures c
                     WHERE c.id_offre_emploi = o.id AND c.etat = 'DEPOSEE') AS nbNouvelles
            FROM offre_emploi o
            JOIN besoin b     ON b.id = o.id_besoin
            JOIN unite_orga u ON u.id = b.id_unite_orga
            ORDER BY o.id DESC
            """)
    List<OffreCandidaturesRow> findOffresAvecCompteurs();

    /** Offres publiées et encore ouvertes, filtrées éventuellement par id et/ou unité (sous-unités incluses). */
    @Query(nativeQuery = true, value = """
            SELECT o.id AS id, o.ref AS ref, o.date_publication AS datePublication,
                   o.date_limite_candidature AS dateLimiteCandidature,
                   tc.libelle  AS typeContrat,
                   u.libelle   AS unite,
                   dir.libelle AS direction,
                   string_agg(DISTINCT p.libelle, ', ' ORDER BY p.libelle) AS postes,
                   count(bp.id) AS nbPostes,
                   (o.id_document IS NOT NULL) AS ficheDisponible
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
            WHERE o.etat = 'PUBLIEE'
              AND (o.date_publication IS NULL OR o.date_publication <= CURRENT_DATE)
              AND (o.date_limite_candidature IS NULL OR o.date_limite_candidature >= CURRENT_DATE)
              AND (cast(:idOffre AS integer) IS NULL OR o.id = :idOffre)
              AND (cast(:idUnite AS integer) IS NULL OR b.id_unite_orga IN (
                  WITH RECURSIVE sous_unites AS (
                      SELECT id FROM unite_orga WHERE id = :idUnite
                      UNION ALL
                      SELECT u2.id FROM unite_orga u2 JOIN sous_unites s ON u2.id_unite_mere = s.id
                  )
                  SELECT id FROM sous_unites))
            GROUP BY o.id, tc.libelle, u.libelle, dir.libelle
            ORDER BY o.date_limite_candidature NULLS LAST, o.id
            """)
    List<OffrePubliqueRow> findOffresVisibles(@Param("idOffre") Integer idOffre, @Param("idUnite") Integer idUnite);

    @Query(nativeQuery = true, value = """
            SELECT count(*) > 0 FROM offre_emploi o
            WHERE o.id = :idOffre AND o.etat = 'PUBLIEE'
              AND (o.date_publication IS NULL OR o.date_publication <= CURRENT_DATE)
              AND (o.date_limite_candidature IS NULL OR o.date_limite_candidature >= CURRENT_DATE)
            """)
    boolean estVisible(@Param("idOffre") Integer idOffre);

    @Query(nativeQuery = true, value = """
            SELECT o.id_document FROM offre_emploi o
            WHERE o.id = :idOffre AND o.id_document IS NOT NULL AND o.etat = 'PUBLIEE'
              AND (o.date_publication IS NULL OR o.date_publication <= CURRENT_DATE)
              AND (o.date_limite_candidature IS NULL OR o.date_limite_candidature >= CURRENT_DATE)
            """)
    Optional<Integer> findIdDocumentVisible(@Param("idOffre") Integer idOffre);

    /** Directions et départements, ordonnés selon l'arborescence. */
    @Query(nativeQuery = true, value = """
            WITH RECURSIVE arbre AS (
                SELECT id, type, libelle, 0 AS profondeur, ARRAY[cast(libelle AS text)] AS chemin
                FROM unite_orga WHERE id_unite_mere IS NULL
                UNION ALL
                SELECT u.id, u.type, u.libelle, a.profondeur + 1, a.chemin || cast(u.libelle AS text)
                FROM unite_orga u JOIN arbre a ON u.id_unite_mere = a.id
            )
            SELECT id AS id, type AS type, libelle AS libelle, profondeur AS profondeur FROM arbre
            WHERE type IN ('DIRECTION', 'DEPARTEMENT')
            ORDER BY chemin
            """)
    List<UniteFiltreRow> findUnitesFiltre();

    interface UniteFiltreRow {
        Integer getId();
        String getType();
        String getLibelle();
        Integer getProfondeur();
    }

    interface OffreCandidaturesRow {
        Integer getId();
        String getRef();
        String getEtat();
        LocalDate getDateLimiteCandidature();
        String getUnite();
        String getPostes();
        Long getNbCandidatures();
        Long getNbNouvelles();
    }

    interface OffrePubliqueRow {
        Integer getId();
        String getRef();
        LocalDate getDatePublication();
        LocalDate getDateLimiteCandidature();
        String getTypeContrat();
        String getUnite();
        String getDirection();
        String getPostes();
        Long getNbPostes();
        Boolean getFicheDisponible();
    }
}
