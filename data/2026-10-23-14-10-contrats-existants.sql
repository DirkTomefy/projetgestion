-- =====================================================================
-- Reprise des contrats du personnel déjà en poste : chaque affectation en cours sans contrat
-- reçoit un contrat SIGNE avec un salaire pris dans la grille validée (catégorie × échelon du poste).
-- CDD de 24 mois pour les postes d'échelon Junior, CDI sinon.
-- Les budgets 2026 sont ensuite portés au niveau de la masse salariale réelle (+ 35 % de marge
-- pour les recrutements), arrondis à 10 millions, sans jamais être baissés.
-- Peut être relancé sans erreur.
-- =====================================================================

CREATE TEMP TABLE reprise_contrat AS
SELECT a.id AS id_affectation, e.id AS id_employe, e.matricule, a.date_debut, p.id_echelon,
       CASE WHEN ec.libelle ILIKE 'junior' THEN 'CDD' ELSE 'CDI' END AS type_libelle,
       -- salaire dans la fourchette : entre 30 % et 70 % de l'écart min-max selon l'employé, arrondi à 50 000 Ar
       ROUND((gd.montant_min + (gd.montant_max - gd.montant_min) * (0.3 + 0.1 * (e.id % 5))) / 50000) * 50000 AS salaire
FROM affectation a
JOIN employe e         ON e.id = a.id_employe
JOIN poste p           ON p.id = a.id_poste
JOIN metier m          ON m.id = p.id_metier
JOIN echelon_metier ec ON ec.id = p.id_echelon
JOIN LATERAL (
    SELECT d.montant_min, d.montant_max FROM grille_salariale g
    JOIN grille_salariale_details d ON d.id_grille = g.id
    WHERE d.id_categorie = m.id_categorie AND d.id_echelon = p.id_echelon AND g.statut = 'VALIDEE'
    ORDER BY (g.date_effectif <= CURRENT_DATE) DESC, g.date_effectif DESC LIMIT 1) gd ON TRUE
WHERE a.date_fin IS NULL
  AND NOT EXISTS (SELECT 1 FROM contrat c WHERE c.id_affectation = a.id
                  AND c.statut_actuel IN ('CREE', 'VALIDE', 'SIGNE'));

INSERT INTO contrat (reference, id_affectation, id_type, date_debut, date_fin_prevue, statut_actuel)
SELECT 'CTR-REP-' || r.matricule, r.id_affectation, t.id, r.date_debut,
       CASE WHEN r.type_libelle = 'CDD' THEN (r.date_debut + INTERVAL '24 months' - INTERVAL '1 day')::date END,
       'SIGNE'
FROM reprise_contrat r JOIN type_contrat t ON t.libelle = r.type_libelle
WHERE NOT EXISTS (SELECT 1 FROM contrat c WHERE c.reference = 'CTR-REP-' || r.matricule);

-- Historique : créé, validé et signé à la date d'entrée
INSERT INTO etat_contrat (id_contrat, statut, date)
SELECT c.id, s.statut, c.date_debut + s.decalage
FROM contrat c JOIN reprise_contrat r ON r.id_affectation = c.id_affectation
CROSS JOIN (VALUES ('CREE', INTERVAL '8 hours'), ('VALIDE', INTERVAL '9 hours'), ('SIGNE', INTERVAL '10 hours')) AS s(statut, decalage)
WHERE c.reference LIKE 'CTR-REP-%'
  AND NOT EXISTS (SELECT 1 FROM etat_contrat x WHERE x.id_contrat = c.id);

INSERT INTO contrat_remuneration (id_contrat, date_effet, salaire, id_echelon)
SELECT c.id, c.date_debut, r.salaire, r.id_echelon
FROM contrat c JOIN reprise_contrat r ON r.id_affectation = c.id_affectation
WHERE c.reference LIKE 'CTR-REP-%'
  AND NOT EXISTS (SELECT 1 FROM contrat_remuneration x WHERE x.id_contrat = c.id);

DROP TABLE reprise_contrat;

-- Budgets 2026 cohérents avec la masse salariale annuelle de chaque unité
UPDATE budget_unite b SET montant = x.montant, montant_propose = NULL, statut = 'VALIDE'
FROM (
    SELECT p.id_unite_orga AS id_unite, CEIL(SUM(cr.salaire) * 12 * 1.35 / 10000000) * 10000000 AS montant
    FROM contrat c
    JOIN affectation a ON a.id = c.id_affectation
    JOIN poste p ON p.id = a.id_poste
    JOIN LATERAL (SELECT salaire FROM contrat_remuneration WHERE id_contrat = c.id ORDER BY date_effet DESC, id DESC LIMIT 1) cr ON TRUE
    WHERE c.statut_actuel IN ('CREE', 'VALIDE', 'SIGNE') AND (a.date_fin IS NULL OR a.date_fin >= CURRENT_DATE)
    GROUP BY p.id_unite_orga
) x
WHERE b.id_unite_orga = x.id_unite AND b.exercice = 2026 AND b.montant < x.montant;
