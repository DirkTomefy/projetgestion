INSERT INTO personne (nom, prenoms, telephone, email, date_naissance)
SELECT v.nom, v.prenoms, v.telephone, v.email, v.date_naissance::date
FROM (VALUES ('Rabe',   'Jean',  '0341111111', 'rabe.test@mail.mg',   '1995-03-02'),
             ('Rasoa',  'Marie', '0342222222', 'rasoa.test@mail.mg',  '1998-07-15'),
             ('Rakoto', 'Paul',  '0343333333', 'rakoto.test@mail.mg', '1993-11-20'))
     AS v(nom, prenoms, telephone, email, date_naissance)
WHERE NOT EXISTS (SELECT 1 FROM personne p WHERE lower(p.email) = v.email);

INSERT INTO candidat (nom, prenom, date_naissance, email, numero, id_personne)
SELECT p.nom, p.prenoms, p.date_naissance, p.email, p.telephone, p.id
FROM personne p
WHERE p.email IN ('rabe.test@mail.mg', 'rasoa.test@mail.mg', 'rakoto.test@mail.mg')
  AND NOT EXISTS (SELECT 1 FROM candidat c WHERE lower(c.email) = lower(p.email));

INSERT INTO candidatures (id_candidat, id_offre_emploi, date_depot, etat)
SELECT c.id, o.id, CURRENT_DATE - 2, 'DEPOSEE'
FROM candidat c
JOIN offre_emploi o ON o.ref = 'OFF-TEST-P5-1'
WHERE c.email IN ('rabe.test@mail.mg', 'rasoa.test@mail.mg', 'rakoto.test@mail.mg')
ON CONFLICT (id_candidat, id_offre_emploi) DO NOTHING;

INSERT INTO histo_candidature (id_candidature, etat_avant, etat_apres, date_changement, commentaire)
SELECT ca.id, NULL, 'DEPOSEE', ca.date_depot, 'Dépôt de la candidature (données de test)'
FROM candidatures ca
WHERE NOT EXISTS (SELECT 1 FROM histo_candidature h WHERE h.id_candidature = ca.id);
