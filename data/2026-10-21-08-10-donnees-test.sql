INSERT INTO motif (libelle)
SELECT 'Test P5 - Surcroit d''activite'
WHERE NOT EXISTS (SELECT 1 FROM motif WHERE libelle = 'Test P5 - Surcroit d''activite');

INSERT INTO besoin (reference, type_besoin, id_motif, id_unite_orga, date_souhaitee, id_user, effectif, etat_actuel)
SELECT v.reference, 'CREATION', m.id, v.unite, CURRENT_DATE + 45, 3, v.effectif, 'OUVERT'
FROM (VALUES ('BES-TEST-P5-1', 4, 2), ('BES-TEST-P5-2', 6, 1), ('BES-TEST-P5-3', 8, 1),
             ('BES-TEST-P5-4', 10, 1), ('BES-TEST-P5-5', 6, 1)) AS v(reference, unite, effectif)
CROSS JOIN (SELECT id FROM motif WHERE libelle = 'Test P5 - Surcroit d''activite') m
ON CONFLICT (reference) DO NOTHING;

INSERT INTO besoin_poste (id_besoin, id_poste, etat)
SELECT b.id, v.poste, 'OUVERT'
FROM (VALUES ('BES-TEST-P5-1', 6), ('BES-TEST-P5-1', 7), ('BES-TEST-P5-2', 9),
             ('BES-TEST-P5-3', 14), ('BES-TEST-P5-4', 17), ('BES-TEST-P5-5', 10)) AS v(reference, poste)
JOIN besoin b ON b.reference = v.reference
WHERE NOT EXISTS (SELECT 1 FROM besoin_poste bp WHERE bp.id_besoin = b.id AND bp.id_poste = v.poste);

INSERT INTO offre_emploi (id_besoin, ref, id_type_contrat, date_publication, date_limite_candidature, etat)
SELECT b.id, v.ref, (SELECT id FROM type_contrat WHERE libelle = v.contrat), v.pub, v.lim, v.etat
FROM (VALUES
        ('BES-TEST-P5-1', 'OFF-TEST-P5-1', 'CE',  CURRENT_DATE - 5,  CURRENT_DATE + 20, 'PUBLIEE'),   -- visible
        ('BES-TEST-P5-2', 'OFF-TEST-P5-2', NULL,  CURRENT_DATE - 2,  CURRENT_DATE,      'PUBLIEE'),   -- dernier jour
        ('BES-TEST-P5-3', 'OFF-TEST-P5-3', 'CDI', CURRENT_DATE - 30, CURRENT_DATE - 1,  'PUBLIEE'),   -- expirée
        ('BES-TEST-P5-4', 'OFF-TEST-P5-4', 'CDD', NULL,              CURRENT_DATE + 10, 'BROUILLON'), -- brouillon
        ('BES-TEST-P5-5', 'OFF-TEST-P5-5', 'CDD', CURRENT_DATE + 3,  CURRENT_DATE + 30, 'PUBLIEE')    -- publiée plus tard
     ) AS v(besoin, ref, contrat, pub, lim, etat)
JOIN besoin b ON b.reference = v.besoin
ON CONFLICT (ref) DO NOTHING;
