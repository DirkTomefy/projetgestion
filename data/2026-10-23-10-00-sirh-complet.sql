-- =====================================================================
-- SIRH complet (blocs 0 à 4 + sprint 3) : comptes et inscription, historique des besoins,
-- contrôle budgétaire, onboarding, coordonnées bancaires, journal d'audit.
-- Peut être relancé sans erreur.
-- =====================================================================

-- ---------------------------------------------------------------- comptes utilisateurs
-- statut : ACTIF (peut se connecter), EN_ATTENTE (inscription à valider), DESACTIVE
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS statut            VARCHAR(20)  NOT NULL DEFAULT 'ACTIF';
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS email             VARCHAR(150) NULL;
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS nom_complet       VARCHAR(200) NULL;
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS date_creation     TIMESTAMP    NOT NULL DEFAULT NOW();
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS derniere_connexion TIMESTAMP   NULL;
ALTER TABLE "user" DROP CONSTRAINT IF EXISTS ck_user_statut;
ALTER TABLE "user" ADD CONSTRAINT ck_user_statut CHECK (statut IN ('ACTIF', 'EN_ATTENTE', 'DESACTIVE'));

-- Nom affiché des comptes existants : celui de l'employé rattaché
UPDATE "user" u SET nom_complet = TRIM(COALESCE(e.prenoms, '') || ' ' || INITCAP(e.nom))
FROM employe e WHERE e.id = u.id_employe AND u.nom_complet IS NULL;

INSERT INTO role (libelle) SELECT 'ADMIN'   WHERE NOT EXISTS (SELECT 1 FROM role WHERE libelle = 'ADMIN');
INSERT INTO role (libelle) SELECT 'Employe' WHERE NOT EXISTS (SELECT 1 FROM role WHERE libelle = 'Employe');

-- Administrateur de l'application (gestion des comptes et des référentiels)
INSERT INTO "user" (login, mot_de_passe, nom_complet, statut)
SELECT 'admin', 'hash_demo_admin', 'Administrateur SIRH', 'ACTIF'
WHERE NOT EXISTS (SELECT 1 FROM "user" WHERE login = 'admin');
INSERT INTO user_role (id_user, id_role)
SELECT u.id, r.id FROM "user" u, role r
WHERE u.login = 'admin' AND r.libelle = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM user_role ur WHERE ur.id_user = u.id AND ur.id_role = r.id);

-- Compte technique des décisions automatiques (contrôle budgétaire) : ne peut pas se connecter
INSERT INTO "user" (login, mot_de_passe, nom_complet, statut)
SELECT 'systeme', '!', 'Contrôle automatique', 'DESACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM "user" WHERE login = 'systeme');

-- Responsables de tâches d'onboarding (paie, support) : comptes « Employe » rattachés à leur poste
INSERT INTO "user" (login, mot_de_passe, id_employe, nom_complet, statut)
SELECT 'paie', 'hash_demo_paie', a.id_employe, TRIM(COALESCE(e.prenoms, '') || ' ' || INITCAP(e.nom)), 'ACTIF'
FROM affectation a JOIN poste p ON p.id = a.id_poste JOIN employe e ON e.id = a.id_employe
WHERE p.code = 'POST-017' AND a.date_fin IS NULL
  AND NOT EXISTS (SELECT 1 FROM "user" WHERE login = 'paie')
LIMIT 1;
INSERT INTO "user" (login, mot_de_passe, id_employe, nom_complet, statut)
SELECT 'support', 'hash_demo_support', a.id_employe, TRIM(COALESCE(e.prenoms, '') || ' ' || INITCAP(e.nom)), 'ACTIF'
FROM affectation a JOIN poste p ON p.id = a.id_poste JOIN employe e ON e.id = a.id_employe
WHERE p.code = 'POST-015' AND a.date_fin IS NULL
  AND NOT EXISTS (SELECT 1 FROM "user" WHERE login = 'support')
LIMIT 1;
INSERT INTO user_role (id_user, id_role)
SELECT u.id, r.id FROM "user" u, role r
WHERE u.login IN ('paie', 'support') AND r.libelle = 'Employe'
  AND NOT EXISTS (SELECT 1 FROM user_role ur WHERE ur.id_user = u.id AND ur.id_role = r.id);

-- ---------------------------------------------------------------- besoins
-- Étape du circuit soumise à un contrôle : BUDGET = validée automatiquement si le coût annuel
-- estimé tient dans le budget de l'unité demandeuse, sinon décision manuelle.
ALTER TABLE workflow_validation ADD COLUMN IF NOT EXISTS controle VARCHAR(20) NULL;
ALTER TABLE workflow_validation DROP CONSTRAINT IF EXISTS ck_workflow_validation_controle;
ALTER TABLE workflow_validation ADD CONSTRAINT ck_workflow_validation_controle CHECK (controle IS NULL OR controle = 'BUDGET');
UPDATE workflow_validation w SET controle = 'BUDGET'
FROM poste p WHERE p.id = w.id_poste AND p.code = 'POST-002' AND w.controle IS NULL;

-- Historique des états du besoin (qui, quand, avant, après, commentaire)
CREATE TABLE IF NOT EXISTS histo_besoin (
    id          SERIAL PRIMARY KEY,
    id_besoin   INT          NOT NULL REFERENCES besoin(id),
    etat_avant  VARCHAR(20)  NULL,
    etat_apres  VARCHAR(20)  NOT NULL,
    action      VARCHAR(50)  NOT NULL,
    id_user     INT          NULL REFERENCES "user"(id),
    date_action TIMESTAMP    NOT NULL DEFAULT NOW(),
    commentaire TEXT         NULL
);
CREATE INDEX IF NOT EXISTS idx_histo_besoin_besoin ON histo_besoin (id_besoin, date_action);

-- Besoins déjà présents : une ligne de création pour ne pas avoir d'historique vide
INSERT INTO histo_besoin (id_besoin, etat_avant, etat_apres, action, id_user, commentaire)
SELECT b.id, NULL, 'EN_ATTENTE', 'CREATION', b.id_user, 'Demande soumise (reprise des données)'
FROM besoin b WHERE NOT EXISTS (SELECT 1 FROM histo_besoin h WHERE h.id_besoin = b.id);

-- ---------------------------------------------------------------- onboarding
ALTER TABLE workflow_onboarding_details ADD COLUMN IF NOT EXISTS libelle VARCHAR(255) NULL;
ALTER TABLE onboarding_details ADD COLUMN IF NOT EXISTS date_echeance DATE NULL;

UPDATE onboarding SET statut = 'EN_COURS' WHERE statut NOT IN ('EN_COURS', 'TERMINE');
ALTER TABLE onboarding DROP CONSTRAINT IF EXISTS ck_onboarding_statut;
ALTER TABLE onboarding ADD CONSTRAINT ck_onboarding_statut CHECK (statut IN ('EN_COURS', 'TERMINE'));
UPDATE onboarding_details SET statut = 'A_FAIRE' WHERE statut NOT IN ('A_FAIRE', 'FAIT', 'NON_APPLICABLE');
ALTER TABLE onboarding_details DROP CONSTRAINT IF EXISTS ck_onboarding_details_statut;
ALTER TABLE onboarding_details ADD CONSTRAINT ck_onboarding_details_statut CHECK (statut IN ('A_FAIRE', 'FAIT', 'NON_APPLICABLE'));
UPDATE workflow_onboarding SET statut = 'ACTIF' WHERE statut IS NULL OR statut NOT IN ('ACTIF', 'INACTIF');
ALTER TABLE workflow_onboarding DROP CONSTRAINT IF EXISTS ck_workflow_onboarding_statut;
ALTER TABLE workflow_onboarding ADD CONSTRAINT ck_workflow_onboarding_statut CHECK (statut IN ('ACTIF', 'INACTIF'));
CREATE UNIQUE INDEX IF NOT EXISTS ux_onboarding_contrat ON onboarding (id_contrat);

-- Modèle par défaut. Poste responsable NULL = le supérieur direct du nouvel employé.
INSERT INTO workflow_onboarding (libelle, statut)
SELECT 'Intégration standard', 'ACTIF'
WHERE NOT EXISTS (SELECT 1 FROM workflow_onboarding WHERE libelle = 'Intégration standard');

INSERT INTO workflow_onboarding_details (id_workflow, ordre, type_tache, libelle, id_poste_responsable, delai_jours, obligatoire)
SELECT w.id, t.ordre, t.type_tache, t.libelle, (SELECT p.id FROM poste p WHERE p.code = t.code_poste), t.delai, t.obligatoire
FROM workflow_onboarding w
CROSS JOIN (VALUES
    (1, 'CONTRAT',    'Classer le contrat signé dans le dossier du personnel', 'POST-018', 0, TRUE),
    (2, 'BADGE',      'Remettre le badge d''accès',                           'POST-003', 1, TRUE),
    (3, 'COMPTE_SI',  'Créer le compte informatique et la messagerie',        'POST-004', 1, TRUE),
    (4, 'MATERIEL',   'Remettre le matériel (poste de travail, téléphone)',   'POST-015', 2, TRUE),
    (5, 'FORMATION',  'Accueil au poste et formation de prise de fonction',   NULL,       5, TRUE),
    (6, 'PAIE',       'Inscrire le salarié à la paie',                        'POST-017', 7, TRUE),
    (7, 'BANQUE',     'Enregistrer les coordonnées bancaires',                'POST-017', 7, FALSE)
) AS t(ordre, type_tache, libelle, code_poste, delai, obligatoire)
WHERE w.libelle = 'Intégration standard'
  AND NOT EXISTS (SELECT 1 FROM workflow_onboarding_details d WHERE d.id_workflow = w.id);

-- ---------------------------------------------------------------- coordonnées bancaires
CREATE TABLE IF NOT EXISTS coordonnees_bancaires (
    id          SERIAL PRIMARY KEY,
    id_employe  INT          NOT NULL REFERENCES employe(id),
    banque      VARCHAR(150) NOT NULL,
    agence      VARCHAR(150) NULL,
    titulaire   VARCHAR(200) NOT NULL,
    rib         VARCHAR(50)  NOT NULL,
    date_effet  DATE         NOT NULL DEFAULT CURRENT_DATE,
    actif       BOOLEAN      NOT NULL DEFAULT TRUE
);
CREATE INDEX IF NOT EXISTS idx_coordonnees_bancaires_employe ON coordonnees_bancaires (id_employe);

-- ---------------------------------------------------------------- journal d'audit
CREATE TABLE IF NOT EXISTS journal_audit (
    id          SERIAL PRIMARY KEY,
    date_action TIMESTAMP    NOT NULL DEFAULT NOW(),
    id_user     INT          NULL REFERENCES "user"(id),
    login       VARCHAR(100) NULL,
    action      VARCHAR(200) NOT NULL,
    methode     VARCHAR(10)  NOT NULL,
    chemin      VARCHAR(300) NOT NULL,
    statut_http INT          NOT NULL,
    adresse_ip  VARCHAR(60)  NULL
);
CREATE INDEX IF NOT EXISTS idx_journal_audit_date ON journal_audit (date_action DESC);
