-- =====================================================================
-- DONNEES DE TEST : propositions d'embauche
-- A executer apres le schema, les migrations et les donnees de base.
-- Cas couverts : proposition en attente, acceptee et refusee.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Points de condition utilises par les propositions
-- ---------------------------------------------------------------------
INSERT INTO point_condition (libelle)
VALUES
    ('SALAIRE'),
    ('DATE_DEBUT'),
    ('HORAIRE'),
    ('LIEU'),
    ('CONDITIONS'),
    ('TYPE_CONTRAT')
ON CONFLICT (libelle) DO NOTHING;

-- Types de contrat necessaires aux offres de test.
INSERT INTO type_contrat (libelle, duree_max, renouvellement_max)
VALUES
    ('CE', 6, NULL),
    ('CDI', NULL, NULL),
    ('CDD', 24, 2)
ON CONFLICT (libelle) DO NOTHING;

-- ---------------------------------------------------------------------
-- 2. Personnes et candidats
-- La colonne candidat.id_personne est ajoutee par migration-v1.
-- ---------------------------------------------------------------------
INSERT INTO personne (nom, prenoms, adresse, telephone, date_naissance, cin, email)
SELECT v.nom, v.prenoms, v.adresse, v.telephone, v.date_naissance, v.cin, v.email
FROM (VALUES
    ('RAKOTO',      'Mialy',   'Antananarivo', '034 00 111 01', DATE '1997-04-12', 'CIN-TEST-001', 'mialy.rakoto@test.local'),
    ('RANDRIA',     'Hery',    'Ambohimangakely', '034 00 111 02', DATE '1994-09-28', 'CIN-TEST-002', 'hery.randria@test.local'),
    ('RAZAFY',      'Soa',     'Ivandry',      '034 00 111 03', DATE '1998-01-17', 'CIN-TEST-003', 'soa.razafy@test.local')
) AS v(nom, prenoms, adresse, telephone, date_naissance, cin, email)
WHERE NOT EXISTS (SELECT 1 FROM personne p WHERE p.cin = v.cin);

INSERT INTO candidat (nom, prenom, date_naissance, adresse, email, numero, id_personne)
SELECT p.nom, p.prenoms, p.date_naissance, p.adresse, p.email, p.telephone, p.id
FROM personne p
WHERE p.cin IN ('CIN-TEST-001', 'CIN-TEST-002', 'CIN-TEST-003')
  AND NOT EXISTS (SELECT 1 FROM candidat c WHERE c.email = p.email);

-- ---------------------------------------------------------------------
-- 3. Offres rattachees aux besoins et aux types de contrat par id
-- ---------------------------------------------------------------------
INSERT INTO offre_emploi
    (id_besoin, ref, id_type_contrat, date_publication, date_limite_candidature, etat)
SELECT b.id, v.offre_ref, tc.id, v.date_publication, v.date_limite, 'PUBLIEE'
FROM (VALUES
    ('BES-TEST-P5-1', 'OFF-TEST-P5-1', (SELECT id FROM type_contrat WHERE libelle = 'CDD'), CURRENT_DATE - 5, CURRENT_DATE + 20),
    ('BES-TEST-P5-2', 'OFF-TEST-P5-2', (SELECT id FROM type_contrat WHERE libelle = 'CE'),  CURRENT_DATE - 2, CURRENT_DATE),
    ('BES-TEST-P5-3', 'OFF-TEST-P5-3', (SELECT id FROM type_contrat WHERE libelle = 'CDI'), CURRENT_DATE - 30, CURRENT_DATE - 1)
) AS v(besoin_ref, offre_ref, id_type_contrat, date_publication, date_limite)
JOIN besoin b ON b.reference = v.besoin_ref
JOIN type_contrat tc ON tc.id = v.id_type_contrat
WHERE NOT EXISTS (SELECT 1 FROM offre_emploi o WHERE o.ref = v.offre_ref);

-- ---------------------------------------------------------------------
-- 4. Candidatures rattachees aux offres de test
-- ---------------------------------------------------------------------
INSERT INTO candidatures (id_candidat, id_offre_emploi, date_depot, etat)
SELECT c.id, o.id, v.date_depot, 'PRESELECTIONNER'
FROM (VALUES
    ('mialy.rakoto@test.local', 'OFF-TEST-P5-1', CURRENT_DATE - 3),
    ('hery.randria@test.local',  'OFF-TEST-P5-2', CURRENT_DATE - 1),
    ('soa.razafy@test.local',   'OFF-TEST-P5-3', CURRENT_DATE - 12)
) AS v(email, offre_ref, date_depot)
JOIN candidat c ON c.email = v.email
JOIN offre_emploi o ON o.ref = v.offre_ref
WHERE NOT EXISTS (
    SELECT 1
    FROM candidatures ca
    WHERE ca.id_candidat = c.id AND ca.id_offre_emploi = o.id
);

-- ---------------------------------------------------------------------
-- 5. Propositions
-- ---------------------------------------------------------------------
INSERT INTO proposition_embauche
    (id_candidature, id_statut_actuel, id_besoin_poste, date_limite_reponse)
SELECT ca.id, v.statut, bp.id, v.date_limite
FROM (VALUES
    ('mialy.rakoto@test.local', 'OFF-TEST-P5-1', 'BES-TEST-P5-1', 6,  'PROPOSER', CURRENT_DATE + 7),
    ('hery.randria@test.local',  'OFF-TEST-P5-2', 'BES-TEST-P5-2', 9,  'ACCEPTER', CURRENT_DATE - 2),
    ('soa.razafy@test.local',   'OFF-TEST-P5-3', 'BES-TEST-P5-3', 14, 'REFUSER',  CURRENT_DATE - 5)
) AS v(email, offre_ref, besoin_ref, poste_id, statut, date_limite)
JOIN candidat c ON c.email = v.email
JOIN offre_emploi o ON o.ref = v.offre_ref
JOIN candidatures ca ON ca.id_candidat = c.id AND ca.id_offre_emploi = o.id
JOIN besoin b ON b.reference = v.besoin_ref
JOIN besoin_poste bp ON bp.id_besoin = b.id AND bp.id_poste = v.poste_id
WHERE NOT EXISTS (
    SELECT 1
    FROM proposition_embauche pe
    WHERE pe.id_candidature = ca.id AND pe.id_besoin_poste = bp.id
);

-- ---------------------------------------------------------------------
-- 6. Conditions de chaque proposition
-- ---------------------------------------------------------------------
INSERT INTO proposition_condition (id_proposition, id_point_condition, valeur, accepte)
SELECT pe.id, pc.id,
       CASE pc.libelle
           WHEN 'SALAIRE'      THEN v.salaire
           WHEN 'DATE_DEBUT'   THEN v.date_debut
           WHEN 'HORAIRE'      THEN 'Lundi au vendredi, 08:00-17:00'
           WHEN 'LIEU'         THEN v.lieu
           WHEN 'CONDITIONS'   THEN 'Periode d''essai de 3 mois'
           WHEN 'TYPE_CONTRAT' THEN v.id_type_contrat::TEXT
       END,
       CASE
           WHEN pe.id_statut_actuel = 'PROPOSER' THEN NULL
           ELSE pe.id_statut_actuel = 'ACCEPTER'
       END
FROM proposition_embauche pe
JOIN candidatures ca ON ca.id = pe.id_candidature
JOIN candidat c ON c.id = ca.id_candidat
JOIN (VALUES
    ('mialy.rakoto@test.local', '1800000 MGA', '2026-12-01', 'Direction des systemes d''information', (SELECT id FROM type_contrat WHERE libelle = 'CDD')),
    ('hery.randria@test.local',  '2200000 MGA', '2026-11-15', 'Departement Developpement',              (SELECT id FROM type_contrat WHERE libelle = 'CE')),
    ('soa.razafy@test.local',   '2500000 MGA', '2026-11-20', 'Service Reseau',                         (SELECT id FROM type_contrat WHERE libelle = 'CDI'))
) AS v(email, salaire, date_debut, lieu, id_type_contrat) ON v.email = c.email
CROSS JOIN point_condition pc
WHERE NOT EXISTS (
    SELECT 1
    FROM proposition_condition pcond
    WHERE pcond.id_proposition = pe.id AND pcond.id_point_condition = pc.id
);

-- ---------------------------------------------------------------------
-- 7. Historique des statuts
-- ---------------------------------------------------------------------
INSERT INTO histo_proposition_embauche (id_proposition_embauche, id_statut, date)
SELECT pe.id, h.statut, h.date_statut
FROM proposition_embauche pe
JOIN candidatures ca ON ca.id = pe.id_candidature
JOIN candidat c ON c.id = ca.id_candidat
JOIN (VALUES
    ('mialy.rakoto@test.local', 'PROPOSER', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    ('hery.randria@test.local',  'PROPOSER', CURRENT_TIMESTAMP - INTERVAL '5 days'),
    ('hery.randria@test.local',  'ACCEPTER', CURRENT_TIMESTAMP - INTERVAL '2 days'),
    ('soa.razafy@test.local',   'PROPOSER', CURRENT_TIMESTAMP - INTERVAL '8 days'),
    ('soa.razafy@test.local',   'REFUSER',  CURRENT_TIMESTAMP - INTERVAL '5 days')
) AS h(email, statut, date_statut) ON h.email = c.email
WHERE NOT EXISTS (
    SELECT 1
    FROM histo_proposition_embauche hp
    WHERE hp.id_proposition_embauche = pe.id AND hp.id_statut = h.statut
);

-- Les insertions explicites ne fixent pas les ids ; les sequences restent donc coherentes.
