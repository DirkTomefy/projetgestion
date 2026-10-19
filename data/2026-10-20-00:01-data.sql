-- =====================================================================
-- DONNÉES DE TEST (PostgreSQL) - à exécuter après schema.sql
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Unités organisationnelles (arbre varié)
--
-- 1  Direction generale
-- ├── 2  Direction financiere
-- │    └── 4  Service Compta                 (service directement sous une direction)
-- ├── 3  DRH
-- │    └── 10 Service Paie                   (service directement sous une direction)
-- └── 5  Direction SI
--      ├── 6  Departement Developpement      (departement sans service)
--      └── 7  Departement Infrastructure
--           └── 8  Service Reseau
--                └── 9  Section Support      (chaine complete jusqu'a la section)
-- ---------------------------------------------------------------------
INSERT INTO unite_orga (id, code, type, libelle, id_unite_mere) VALUES
 (1,  'DG',          'DIRECTION',   'Direction generale',            NULL),
 (2,  'DAF',         'DIRECTION',   'Direction financiere',          1),
 (3,  'DRH',         'DIRECTION',   'DRH',                           1),
 (4,  'S-COMPTA',    'SERVICE',     'Service Compta',                2),
 (5,  'DSI',         'DIRECTION',   'Direction des systemes d''information', 1),
 (6,  'DEP-DEV',     'DEPARTEMENT', 'Departement Developpement',     5),
 (7,  'DEP-INFRA',   'DEPARTEMENT', 'Departement Infrastructure',    5),
 (8,  'S-RESEAU',    'SERVICE',     'Service Reseau',                7),
 (9,  'SEC-SUPPORT', 'SECTION',     'Section Support',               8),
 (10, 'S-PAIE',      'SERVICE',     'Service Paie',                  3);

-- ---------------------------------------------------------------------
-- 2. Catégories, échelons, métiers
-- ---------------------------------------------------------------------
INSERT INTO categorie_metier (id, libelle) VALUES
 (1, 'Cadre'),
 (2, 'Agent de maitrise'),
 (3, 'Employe');

INSERT INTO echelon_metier (id, libelle) VALUES
 (1, 'Junior'),
 (2, 'Confirme'),
 (3, 'Senior');

INSERT INTO metier (id, libelle, id_categorie) VALUES
 (1,  'Directeur general',        1),
 (2,  'Directeur',                1),
 (3,  'Chef de departement',      1),
 (4,  'Chef de service',          2),
 (5,  'Comptable',                2),
 (6,  'Developpeur',              2),
 (7,  'Administrateur reseau',    2),
 (8,  'Technicien support',       3),
 (9,  'Gestionnaire de paie',     2),
 (10, 'Charge de recrutement',    2);

-- ---------------------------------------------------------------------
-- 3. Postes (tous ACTIF, tous LIBRE : aucun employé n'est encore affecté)
-- colonnes : id, code, id_metier, id_echelon, id_unite_orga, id_superieur_direct, libelle
-- ---------------------------------------------------------------------
INSERT INTO poste (id, code, id_metier, id_echelon, id_unite_orga, id_superieur_direct, libelle, etat, statut_occupation) VALUES
 (1,  'POST-001', 1,  3, 1,  NULL, 'Directeur general',                    'ACTIF', 'LIBRE'),
 (2,  'POST-002', 2,  3, 2,  1,    'Directeur financier',                  'ACTIF', 'LIBRE'),
 (3,  'POST-003', 2,  3, 3,  1,    'Directeur des ressources humaines',    'ACTIF', 'LIBRE'),
 (4,  'POST-004', 2,  3, 5,  1,    'Directeur des systemes d''information','ACTIF', 'LIBRE'),
 (5,  'POST-005', 4,  3, 4,  2,    'Chef du service comptabilite',         'ACTIF', 'LIBRE'),
 (6,  'POST-006', 5,  1, 4,  5,    'Comptable junior',                     'ACTIF', 'LIBRE'),
 (7,  'POST-007', 5,  2, 4,  5,    'Comptable confirme',                   'ACTIF', 'LIBRE'),
 (8,  'POST-008', 3,  3, 6,  4,    'Chef du departement developpement',    'ACTIF', 'LIBRE'),
 (9,  'POST-009', 6,  1, 6,  8,    'Developpeur junior',                   'ACTIF', 'LIBRE'),
 (10, 'POST-010', 6,  2, 6,  8,    'Developpeur confirme',                 'ACTIF', 'LIBRE'),
 (11, 'POST-011', 6,  3, 6,  8,    'Developpeur senior',                   'ACTIF', 'LIBRE'),
 (12, 'POST-012', 3,  3, 7,  4,    'Chef du departement infrastructure',   'ACTIF', 'LIBRE'),
 (13, 'POST-013', 4,  2, 8,  12,   'Chef du service reseau',              'ACTIF', 'LIBRE'),
 (14, 'POST-014', 7,  2, 8,  13,   'Administrateur reseau',                'ACTIF', 'LIBRE'),
 (15, 'POST-015', 8,  1, 9,  13,   'Technicien support',                   'ACTIF', 'LIBRE'),
 (16, 'POST-016', 4,  3, 10, 3,    'Chef du service paie',                 'ACTIF', 'LIBRE'),
 (17, 'POST-017', 9,  2, 10, 16,   'Gestionnaire de paie',                 'ACTIF', 'LIBRE'),
 (18, 'POST-018', 10, 2, 3,  3,    'Charge de recrutement',                'ACTIF', 'LIBRE');

-- Responsable de chaque unité
UPDATE unite_orga SET id_poste_responsable = 1  WHERE id = 1;
UPDATE unite_orga SET id_poste_responsable = 2  WHERE id = 2;
UPDATE unite_orga SET id_poste_responsable = 3  WHERE id = 3;
UPDATE unite_orga SET id_poste_responsable = 5  WHERE id = 4;
UPDATE unite_orga SET id_poste_responsable = 4  WHERE id = 5;
UPDATE unite_orga SET id_poste_responsable = 8  WHERE id = 6;
UPDATE unite_orga SET id_poste_responsable = 12 WHERE id = 7;
UPDATE unite_orga SET id_poste_responsable = 13 WHERE id = 8;
UPDATE unite_orga SET id_poste_responsable = 14 WHERE id = 9;
UPDATE unite_orga SET id_poste_responsable = 16 WHERE id = 10;

-- ---------------------------------------------------------------------
-- 4. Grille salariale valide (en Ariary) + budget par unité
-- ---------------------------------------------------------------------
INSERT INTO grille_salariale (id, date_effectif, date_fin) VALUES
 (1, '2026-01-01', NULL);   -- en vigueur, sans date de fin

INSERT INTO grille_salariale_details (id, id_grille, id_categorie, id_echelon, montant_min, montant_max) VALUES
 -- Cadre
 (1, 1, 1, 1, 3000000,  4000000),
 (2, 1, 1, 2, 4000000,  6000000),
 (3, 1, 1, 3, 6000000,  10000000),
 -- Agent de maitrise
 (4, 1, 2, 1, 1500000,  2000000),
 (5, 1, 2, 2, 2000000,  3000000),
 (6, 1, 2, 3, 3000000,  4000000),
 -- Employe
 (7, 1, 3, 1, 800000,   1000000),
 (8, 1, 3, 2, 1000000,  1300000),
 (9, 1, 3, 3, 1300000,  1800000);

INSERT INTO budget_unite (id, id_unite_orga, exercice, montant) VALUES
 (1,  1,  2026, 500000000),
 (2,  2,  2026, 120000000),
 (3,  3,  2026, 90000000),
 (4,  4,  2026, 60000000),
 (5,  5,  2026, 200000000),
 (6,  6,  2026, 110000000),
 (7,  7,  2026, 70000000),
 (8,  8,  2026, 40000000),
 (9,  9,  2026, 25000000),
 (10, 10, 2026, 35000000);

-- ---------------------------------------------------------------------
-- 5. Rôles, utilisateurs et affectation des rôles
-- (mot_de_passe : valeur factice, à remplacer par un vrai hash, ex. bcrypt)
-- ---------------------------------------------------------------------
INSERT INTO role (id, libelle) VALUES
 (1, 'Manager'),
 (2, 'RH'),
 (3, 'Finance'),
 (4, 'DG');

INSERT INTO "user" (id, login, mot_de_passe, id_employe) VALUES
 (1, 'manager.dev',    'hash_demo_manager_dev',    NULL),
 (2, 'manager.compta', 'hash_demo_manager_compta', NULL),
 (3, 'rh.drh',         'hash_demo_rh',             NULL),
 (4, 'finance.daf',    'hash_demo_finance',        NULL),
 (5, 'dg',             'hash_demo_dg',             NULL);

INSERT INTO user_role (id, id_user, id_role) VALUES
 (1, 1, 1),   -- manager.dev    -> Manager
 (2, 2, 1),   -- manager.compta -> Manager
 (3, 3, 2),   -- rh.drh         -> RH
 (4, 4, 3),   -- finance.daf    -> Finance
 (5, 5, 4);   -- dg             -> DG

-- ---------------------------------------------------------------------
-- 6. Remise à niveau des séquences (ids insérés à la main)
-- ---------------------------------------------------------------------
SELECT setval(pg_get_serial_sequence('unite_orga', 'id'),               (SELECT MAX(id) FROM unite_orga));
SELECT setval(pg_get_serial_sequence('categorie_metier', 'id'),         (SELECT MAX(id) FROM categorie_metier));
SELECT setval(pg_get_serial_sequence('echelon_metier', 'id'),           (SELECT MAX(id) FROM echelon_metier));
SELECT setval(pg_get_serial_sequence('metier', 'id'),                   (SELECT MAX(id) FROM metier));
SELECT setval(pg_get_serial_sequence('poste', 'id'),                    (SELECT MAX(id) FROM poste));
SELECT setval(pg_get_serial_sequence('grille_salariale', 'id'),         (SELECT MAX(id) FROM grille_salariale));
SELECT setval(pg_get_serial_sequence('grille_salariale_details', 'id'), (SELECT MAX(id) FROM grille_salariale_details));
SELECT setval(pg_get_serial_sequence('budget_unite', 'id'),             (SELECT MAX(id) FROM budget_unite));
SELECT setval(pg_get_serial_sequence('role', 'id'),                     (SELECT MAX(id) FROM role));
SELECT setval(pg_get_serial_sequence('"user"', 'id'),                   (SELECT MAX(id) FROM "user"));
SELECT setval(pg_get_serial_sequence('user_role', 'id'),                (SELECT MAX(id) FROM user_role));