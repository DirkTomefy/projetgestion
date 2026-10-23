-- =====================================================================
-- Comptes de démonstration pour tous les postes occupés qui n'en avaient pas.
-- Mot de passe = hash_demo_<login avec _ à la place du point> (en clair : re-haché à la 1re connexion).
-- Rôles : RH + Manager pour le DRH, Manager pour les responsables d'unité, Employe pour les autres.
-- Peut être relancé sans erreur.
-- =====================================================================
WITH comptes(code, login, mdp) AS (VALUES
    ('POST-003', 'drh',             'hash_demo_drh'),
    ('POST-004', 'dsi',             'hash_demo_dsi'),
    ('POST-006', 'compta.junior',   'hash_demo_compta_junior'),
    ('POST-007', 'compta.confirme', 'hash_demo_compta_confirme'),
    ('POST-009', 'dev.junior',      'hash_demo_dev_junior'),
    ('POST-010', 'dev.confirme',    'hash_demo_dev_confirme'),
    ('POST-011', 'dev.senior',      'hash_demo_dev_senior'),
    ('POST-012', 'chef.infra',      'hash_demo_chef_infra'),
    ('POST-013', 'chef.reseau',     'hash_demo_chef_reseau'),
    ('POST-014', 'admin.reseau',    'hash_demo_admin_reseau'),
    ('POST-016', 'chef.paie',       'hash_demo_chef_paie')
)
INSERT INTO "user" (login, mot_de_passe, id_employe, nom_complet, statut)
SELECT c.login, c.mdp, a.id_employe, TRIM(COALESCE(e.prenoms, '') || ' ' || INITCAP(e.nom)), 'ACTIF'
FROM comptes c
JOIN poste p ON p.code = c.code
JOIN affectation a ON a.id_poste = p.id AND a.date_fin IS NULL
JOIN employe e ON e.id = a.id_employe
WHERE NOT EXISTS (SELECT 1 FROM "user" u WHERE u.login = c.login)
  AND NOT EXISTS (SELECT 1 FROM "user" u WHERE u.id_employe = a.id_employe);

WITH roles(login, role) AS (VALUES
    ('drh', 'RH'), ('drh', 'Manager'),
    ('dsi', 'Manager'), ('chef.infra', 'Manager'), ('chef.reseau', 'Manager'), ('chef.paie', 'Manager'),
    ('compta.junior', 'Employe'), ('compta.confirme', 'Employe'),
    ('dev.junior', 'Employe'), ('dev.confirme', 'Employe'), ('dev.senior', 'Employe'),
    ('admin.reseau', 'Employe')
)
INSERT INTO user_role (id_user, id_role)
SELECT u.id, r.id FROM roles x
JOIN "user" u ON u.login = x.login
JOIN role r ON r.libelle = x.role
WHERE NOT EXISTS (SELECT 1 FROM user_role ur WHERE ur.id_user = u.id AND ur.id_role = r.id);

-- Niveau commun « Entretien RH » : conduit par le chargé de recrutement (POST-018) et non par le DRH
-- (corrige les bases où le réglage précédent avait mis le DRH).
UPDATE workflow_candidature w
   SET id_poste_responsable = (SELECT id FROM poste WHERE code = 'POST-018')
 WHERE w.id_offre_emploi IS NULL AND w.type_evaluation = 'ENTRETIEN_RH' AND w.responsable_regle = 'POSTE_FIXE'
   AND w.id_poste_responsable = (SELECT id FROM poste WHERE code = 'POST-003')
   AND EXISTS (SELECT 1 FROM poste WHERE code = 'POST-018' AND etat = 'ACTIF');
