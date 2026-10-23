package s5.itu.projetgestion.repository.reporting;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import s5.itu.projetgestion.model.organisation.Employe;

/** Indicateurs agrégés (tableau de bord, budgets, reporting). Lecture seule. */
public interface IndicateurRepository extends Repository<Employe, Integer> {

    /** Ligne générique : libellé → valeur. */
    interface Ligne {
        String getLibelle();
        BigDecimal getValeur();
    }

    /** Indicateurs d'une unité organisationnelle. */
    interface LigneUnite {
        Integer getIdUnite();
        String getCode();
        String getUnite();
        String getType();
        Integer getIdUniteMere();
        Long getPostes();
        Long getOccupes();
        Long getVacants();
        Long getEffectif();
        BigDecimal getMasseMensuelle();
        BigDecimal getBudget();
        BigDecimal getBesoinsEnCours();
    }

    // ------------------------------------------------------------------ chiffres clés

    @Query(value = "SELECT COUNT(DISTINCT a.id_employe) FROM affectation a WHERE a.date_fin IS NULL OR a.date_fin >= CURRENT_DATE", nativeQuery = true)
    long effectif();

    @Query(value = "SELECT COUNT(*) FROM poste WHERE etat = :etat", nativeQuery = true)
    long postesParEtat(@Param("etat") String etat);

    @Query(value = "SELECT COUNT(*) FROM poste WHERE etat = 'ACTIF' AND statut_occupation = 'LIBRE'", nativeQuery = true)
    long postesVacants();

    @Query(value = """
            SELECT COALESCE(SUM(r.salaire), 0)
            FROM contrat c
            JOIN affectation a ON a.id = c.id_affectation
            JOIN LATERAL (SELECT cr.salaire FROM contrat_remuneration cr WHERE cr.id_contrat = c.id
                          ORDER BY cr.date_effet DESC, cr.id DESC LIMIT 1) r ON TRUE
            WHERE c.statut_actuel IN ('CREE', 'VALIDE', 'SIGNE') AND (a.date_fin IS NULL OR a.date_fin >= CURRENT_DATE)
            """, nativeQuery = true)
    BigDecimal masseSalarialeMensuelle();

    @Query(value = "SELECT COALESCE(SUM(montant), 0) FROM budget_unite WHERE exercice = :exercice", nativeQuery = true)
    BigDecimal budgetTotal(@Param("exercice") int exercice);

    @Query(value = "SELECT COUNT(*) FROM candidatures WHERE date_depot >= CURRENT_DATE - :jours", nativeQuery = true)
    long candidaturesRecentes(@Param("jours") int jours);

    @Query(value = "SELECT COUNT(*) FROM offre_emploi WHERE etat = 'PUBLIEE'", nativeQuery = true)
    long offresPubliees();

    @Query(value = "SELECT COUNT(*) FROM proposition_embauche WHERE id_statut_actuel = 'PROPOSER'", nativeQuery = true)
    long propositionsEnAttente();

    @Query(value = """
            SELECT COUNT(*) FROM contrat
            WHERE statut_actuel IN ('VALIDE', 'SIGNE') AND date_fin_prevue IS NOT NULL
              AND date_fin_prevue BETWEEN CURRENT_DATE AND CURRENT_DATE + :jours
            """, nativeQuery = true)
    long contratsAEcheance(@Param("jours") int jours);

    @Query(value = "SELECT COUNT(*) FROM onboarding WHERE statut = 'EN_COURS'", nativeQuery = true)
    long onboardingsEnCours();

    @Query(value = """
            SELECT COUNT(*) FROM onboarding_details d JOIN onboarding o ON o.id = d.id_onboarding
            WHERE o.statut = 'EN_COURS' AND d.statut = 'A_FAIRE' AND d.date_echeance < CURRENT_DATE
            """, nativeQuery = true)
    long tachesOnboardingEnRetard();

    // ------------------------------------------------------------------ répartitions

    @Query(value = """
            SELECT CASE etat_actuel WHEN 'EN_ATTENTE' THEN 'En validation' WHEN 'OUVERT' THEN 'Recrutement ouvert'
                                    WHEN 'POURVU' THEN 'Pourvu' ELSE 'Annulé / refusé' END AS "libelle",
                   COUNT(*) AS "valeur"
            FROM besoin GROUP BY 1 ORDER BY 1
            """, nativeQuery = true)
    List<Ligne> besoinsParEtat();

    /** Entonnoir : candidatures déposées, présélectionnées (ou plus loin), retenues, embauchées. */
    @Query(value = """
            SELECT 'Déposées' AS "libelle", COUNT(*) AS "valeur" FROM candidatures
            UNION ALL SELECT 'Présélectionnées', COUNT(*) FROM candidatures c
                WHERE c.etat IN ('PRESELECTIONNER', 'RETENUE') OR EXISTS (SELECT 1 FROM traitement t WHERE t.id_candidature = c.id)
            UNION ALL SELECT 'Retenues', COUNT(*) FROM candidatures WHERE etat = 'RETENUE'
            UNION ALL SELECT 'Embauchées', COUNT(*) FROM proposition_embauche WHERE id_statut_actuel = 'ACCEPTER'
            """, nativeQuery = true)
    List<Ligne> entonnoirRecrutement();

    @Query(value = """
            SELECT CASE etat WHEN 'DEPOSEE' THEN 'Déposée' WHEN 'PRESELECTIONNER' THEN 'En sélection'
                             WHEN 'RETENUE' THEN 'Retenue' WHEN 'REFUSE' THEN 'Refusée' ELSE 'Désistement' END AS "libelle",
                   COUNT(*) AS "valeur"
            FROM candidatures GROUP BY 1 ORDER BY 1
            """, nativeQuery = true)
    List<Ligne> candidaturesParEtat();

    @Query(value = """
            SELECT t.libelle AS "libelle", COUNT(*) AS "valeur"
            FROM contrat c JOIN type_contrat t ON t.id = c.id_type
            WHERE c.statut_actuel IN ('CREE', 'VALIDE', 'SIGNE')
            GROUP BY t.libelle ORDER BY t.libelle
            """, nativeQuery = true)
    List<Ligne> contratsParType();

    @Query(value = """
            SELECT cm.libelle AS "libelle", COUNT(DISTINCT a.id_employe) AS "valeur"
            FROM affectation a JOIN poste p ON p.id = a.id_poste JOIN metier m ON m.id = p.id_metier
            JOIN categorie_metier cm ON cm.id = m.id_categorie
            WHERE a.date_fin IS NULL OR a.date_fin >= CURRENT_DATE
            GROUP BY cm.libelle ORDER BY 2 DESC
            """, nativeQuery = true)
    List<Ligne> effectifParCategorie();

    /** Embauches (contrats initiaux issus d'une proposition) par mois, sur les 12 derniers mois. */
    @Query(value = """
            SELECT to_char(m.mois, 'YYYY-MM') AS "libelle", COUNT(c.id) AS "valeur"
            FROM generate_series(date_trunc('month', CURRENT_DATE) - INTERVAL '11 months', date_trunc('month', CURRENT_DATE), INTERVAL '1 month') AS m(mois)
            LEFT JOIN contrat c ON c.id_proposition IS NOT NULL AND c.id_contrat_precedent IS NULL
                 AND c.statut_actuel <> 'ANNULE' AND date_trunc('month', c.date_debut) = m.mois
            GROUP BY m.mois ORDER BY m.mois
            """, nativeQuery = true)
    List<Ligne> embauchesParMois();

    /** Délai moyen en jours entre la demande (besoin) et l'acceptation de la proposition. */
    @Query(value = """
            SELECT AVG(EXTRACT(EPOCH FROM (pe.date_reponse - d.debut)) / 86400)
            FROM proposition_embauche pe
            JOIN besoin_poste bp ON bp.id = pe.id_besoin_poste
            JOIN (SELECT id_besoin, MIN(date) AS debut FROM validation GROUP BY id_besoin) d ON d.id_besoin = bp.id_besoin
            WHERE pe.id_statut_actuel = 'ACCEPTER' AND pe.date_reponse IS NOT NULL
            """, nativeQuery = true)
    BigDecimal delaiMoyenRecrutementJours();

    // ------------------------------------------------------------------ par unité

    @Query(value = """
            SELECT u.id AS "idUnite", u.code AS "code", u.libelle AS "unite", u.type AS "type", u.id_unite_mere AS "idUniteMere",
                   (SELECT COUNT(*) FROM poste p WHERE p.id_unite_orga = u.id AND p.etat = 'ACTIF') AS "postes",
                   (SELECT COUNT(*) FROM poste p WHERE p.id_unite_orga = u.id AND p.etat = 'ACTIF' AND p.statut_occupation = 'OCCUPE') AS "occupes",
                   (SELECT COUNT(*) FROM poste p WHERE p.id_unite_orga = u.id AND p.etat = 'ACTIF' AND p.statut_occupation = 'LIBRE') AS "vacants",
                   (SELECT COUNT(DISTINCT a.id_employe) FROM affectation a JOIN poste p ON p.id = a.id_poste
                     WHERE p.id_unite_orga = u.id AND (a.date_fin IS NULL OR a.date_fin >= CURRENT_DATE)) AS "effectif",
                   (SELECT COALESCE(SUM(r.salaire), 0) FROM contrat c
                      JOIN affectation a ON a.id = c.id_affectation JOIN poste p ON p.id = a.id_poste
                      JOIN LATERAL (SELECT cr.salaire FROM contrat_remuneration cr WHERE cr.id_contrat = c.id
                                    ORDER BY cr.date_effet DESC, cr.id DESC LIMIT 1) r ON TRUE
                     WHERE p.id_unite_orga = u.id AND c.statut_actuel IN ('CREE', 'VALIDE', 'SIGNE')
                       AND (a.date_fin IS NULL OR a.date_fin >= CURRENT_DATE)) AS "masseMensuelle",
                   (SELECT b.montant FROM budget_unite b WHERE b.id_unite_orga = u.id AND b.exercice = :exercice) AS "budget",
                   (SELECT COALESCE(SUM(be.budget_estime), 0) FROM besoin be
                     WHERE be.id_unite_orga = u.id AND be.etat_actuel IN ('EN_ATTENTE', 'OUVERT')) AS "besoinsEnCours"
            FROM unite_orga u
            ORDER BY u.id
            """, nativeQuery = true)
    List<LigneUnite> parUnite(@Param("exercice") int exercice);
}
