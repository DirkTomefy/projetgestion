-- ---------------------------------------------------------------------
-- 7. Employés et affectations
-- ---------------------------------------------------------------------

INSERT INTO employe (id, matricule, nom, prenoms, adresse) VALUES
 (1,  'EMP-2026-001', 'RAKOTO',      'Jean Michel',       'Antananarivo'),
 (2,  'EMP-2026-002', 'RANDRIAMAMPIANINA', 'Sarah',       'Antananarivo'),
 (3,  'EMP-2026-003', 'RAZAFINDRAKOTO', 'Hery',            'Ambohimangakely'),
 (4,  'EMP-2026-004', 'RABEMANANJARA', 'Miora',            'Ivandry'),
 (5,  'EMP-2026-005', 'RAKOTONDRABE', 'Tojo',              'Ankorondrano'),
 (6,  'EMP-2026-006', 'ANDRIANASOLO', 'Feno',               'Soanierana'),
 (7,  'EMP-2026-007', 'RASOLOFOARISOA', 'Nantenaina',       'Tanjombato'),
 (8,  'EMP-2026-008', 'RAZAFIMALALA', 'Mamy',               'Andraharo'),
 (9,  'EMP-2026-009', 'RANDRIANARISOA', 'Lova',             'Ambohidratrimo'),
 (10, 'EMP-2026-010', 'RAKOTOMALALA', 'Tiana',              'Alarobia'),
 (11, 'EMP-2026-011', 'RAZAFINDRAIBE', 'Tsiry',             'Ankadifotsy'),
 (12, 'EMP-2026-012', 'RABARY',       'Nirina',             'Mahamasina'),
 (13, 'EMP-2026-013', 'ANDRIAMBOLOLONA', 'Fara',            'Tsaralalana'),
 (14, 'EMP-2026-014', 'RAKOTOARISOA', 'Haja',               'Behoririka'),
 (15, 'EMP-2026-015', 'RAZANADRAKOTO', 'Aina',              'Ambodimita'),
 (16, 'EMP-2026-016', 'RANDRIANANTENAINA', 'Voahirana',     'Anosizato'),
 (17, 'EMP-2026-017', 'RAKOTONIAINA', 'Lanto',              'Fianarantsoa'),
 (18, 'EMP-2026-018', 'ANDRIANJAFY', 'Hasina',              'Antsirabe');

INSERT INTO affectation (id, id_poste, id_employe, date_debut, date_fin) VALUES
 (1,  1,  1,  '2026-01-02', NULL),
 (2,  2,  2,  '2026-01-05', NULL),
 (3,  3,  3,  '2026-01-05', NULL),
 (4,  4,  4,  '2026-01-10', NULL),
 (5,  5,  5,  '2026-01-12', NULL),
 (6,  6,  6,  '2026-01-15', NULL),
 (7,  7,  7,  '2026-01-15', NULL),
 (8,  8,  8,  '2026-01-20', NULL),
 (9,  9,  9,  '2026-01-22', NULL),
 (10, 10, 10, '2026-01-22', NULL),
 (11, 11, 11, '2026-01-25', NULL),
 (12, 12, 12, '2026-01-25', NULL),
 (13, 13, 13, '2026-02-01', NULL),
 (14, 14, 14, '2026-02-03', NULL),
 (15, 15, 15, '2026-02-03', NULL),
 (16, 16, 16, '2026-02-05', NULL),
 (17, 17, 17, '2026-02-05', NULL),
 (18, 18, 18, '2026-02-10', NULL);

UPDATE poste
SET statut_occupation = 'OCCUPE'
WHERE id BETWEEN 1 AND 18;

-- Association des comptes utilisateurs aux employés
UPDATE "user" SET id_employe = 8  WHERE id = 1; -- manager.dev
UPDATE "user" SET id_employe = 5  WHERE id = 2; -- manager.compta
UPDATE "user" SET id_employe = 18 WHERE id = 3; -- RH
UPDATE "user" SET id_employe = 2  WHERE id = 4; -- Finance
UPDATE "user" SET id_employe = 1  WHERE id = 5; -- DG

-- Remise à niveau des séquences
SELECT setval(pg_get_serial_sequence('employe', 'id'),
              (SELECT MAX(id) FROM employe));

SELECT setval(pg_get_serial_sequence('affectation', 'id'),
              (SELECT MAX(id) FROM affectation));