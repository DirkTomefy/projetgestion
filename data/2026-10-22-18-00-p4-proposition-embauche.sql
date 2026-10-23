-- =====================================================================
-- P4 – Sprint 2 : proposition d'embauche et clôture des offres
-- Blocs 2 et 3
-- Script idempotent : peut être relancé sans erreur
-- =====================================================================

BEGIN;

-- ---------------------------------------------------------------------
-- 1. Référentiel des conditions d'embauche
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


-- ---------------------------------------------------------------------
-- 2. Proposition d'embauche : jeton et dates de suivi
-- ---------------------------------------------------------------------

ALTER TABLE proposition_embauche
    ADD COLUMN IF NOT EXISTS jeton VARCHAR(64) NULL;

ALTER TABLE proposition_embauche
    ADD COLUMN IF NOT EXISTS date_envoi TIMESTAMP NULL;

ALTER TABLE proposition_embauche
    ADD COLUMN IF NOT EXISTS date_reponse TIMESTAMP NULL;

-- Générer un jeton pour les propositions existantes
UPDATE proposition_embauche
SET jeton = md5(
    random()::text ||
    clock_timestamp()::text ||
    id::text
)
WHERE jeton IS NULL;

ALTER TABLE proposition_embauche
    ALTER COLUMN jeton SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_proposition_embauche_jeton
    ON proposition_embauche (jeton);

-- Statuts autorisés
ALTER TABLE proposition_embauche
    DROP CONSTRAINT IF EXISTS proposition_embauche_id_statut_actuel_check;

ALTER TABLE proposition_embauche
    ADD CONSTRAINT proposition_embauche_id_statut_actuel_check
    CHECK (
        id_statut_actuel IN (
            'PROPOSER',
            'REFUSER',
            'ACCEPTER',
            'EXPIREE'
        )
    );

-- Un besoin de poste ne peut avoir qu'une proposition
-- en cours ou acceptée à la fois.
CREATE UNIQUE INDEX IF NOT EXISTS ux_proposition_siege_en_cours
    ON proposition_embauche (id_besoin_poste)
    WHERE id_statut_actuel IN ('PROPOSER', 'ACCEPTER');

-- Une candidature ne peut avoir qu'une proposition
-- en cours ou acceptée à la fois.
CREATE UNIQUE INDEX IF NOT EXISTS ux_proposition_candidature_en_cours
    ON proposition_embauche (id_candidature)
    WHERE id_statut_actuel IN ('PROPOSER', 'ACCEPTER');


-- ---------------------------------------------------------------------
-- 3. Réponse du candidat condition par condition
-- ---------------------------------------------------------------------

ALTER TABLE proposition_condition
    ADD COLUMN IF NOT EXISTS commentaire_candidat TEXT NULL;


-- ---------------------------------------------------------------------
-- 4. Contre-propositions sur les conditions
-- Conservation de l'ancienne valeur pour l'historique.
-- ---------------------------------------------------------------------

ALTER TABLE relance_condition
    ADD COLUMN IF NOT EXISTS ancienne_valeur TEXT NULL;

ALTER TABLE relance_condition
    ADD COLUMN IF NOT EXISTS id_user INT NULL
    REFERENCES "user"(id);

ALTER TABLE relance_condition
    ADD COLUMN IF NOT EXISTS date_relance TIMESTAMP
    NOT NULL DEFAULT now();

ALTER TABLE relance_condition
    ADD COLUMN IF NOT EXISTS commentaire TEXT NULL;

-- Normaliser les valeurs NULL existantes
UPDATE relance_condition
SET statut = 'EN_ATTENTE'
WHERE statut IS NULL;

ALTER TABLE relance_condition
    DROP CONSTRAINT IF EXISTS ck_relance_condition_statut;

ALTER TABLE relance_condition
    ADD CONSTRAINT ck_relance_condition_statut
    CHECK (
        statut IN (
            'EN_ATTENTE',
            'ACCEPTEE',
            'REFUSEE'
        )
    );

CREATE INDEX IF NOT EXISTS idx_relance_condition
    ON relance_condition (id_proposition_condition);


-- ---------------------------------------------------------------------
-- 5. Historique des statuts des propositions
-- Qui a effectué l'action et pourquoi ?
-- id_user NULL : candidat ou tâche automatique.
-- ---------------------------------------------------------------------

ALTER TABLE histo_proposition_embauche
    ADD COLUMN IF NOT EXISTS id_user INT NULL
    REFERENCES "user"(id);

ALTER TABLE histo_proposition_embauche
    ADD COLUMN IF NOT EXISTS commentaire TEXT NULL;

ALTER TABLE histo_proposition_embauche
    DROP CONSTRAINT IF EXISTS histo_proposition_embauche_id_statut_check;

ALTER TABLE histo_proposition_embauche
    ADD CONSTRAINT histo_proposition_embauche_id_statut_check
    CHECK (
        id_statut IN (
            'PROPOSER',
            'REFUSER',
            'ACCEPTER',
            'EXPIREE'
        )
    );

CREATE INDEX IF NOT EXISTS idx_histo_proposition
    ON histo_proposition_embauche (
        id_proposition_embauche,
        date
    );


-- ---------------------------------------------------------------------
-- 6. Clôture des offres d'emploi
-- DATE_LIMITE : clôture automatique après échéance.
-- POSTES_POURVUS : tous les postes sont pourvus.
-- MANUELLE : clôture par un utilisateur RH.
-- ---------------------------------------------------------------------

ALTER TABLE offre_emploi
    ADD COLUMN IF NOT EXISTS date_cloture TIMESTAMP NULL;

ALTER TABLE offre_emploi
    ADD COLUMN IF NOT EXISTS motif_cloture VARCHAR(20) NULL;

ALTER TABLE offre_emploi
    ADD COLUMN IF NOT EXISTS id_user_cloture INT NULL
    REFERENCES "user"(id);

ALTER TABLE offre_emploi
    DROP CONSTRAINT IF EXISTS ck_offre_emploi_motif_cloture;

ALTER TABLE offre_emploi
    ADD CONSTRAINT ck_offre_emploi_motif_cloture
    CHECK (
        motif_cloture IS NULL
        OR motif_cloture IN (
            'DATE_LIMITE',
            'POSTES_POURVUS',
            'MANUELLE'
        )
    );


-- ---------------------------------------------------------------------
-- 7. Conversion des valeurs TYPE_CONTRAT
-- Les valeurs sont remplacées par l'identifiant du type de contrat.
-- Exemples : CE, CDD, CDI, selon les libellés présents en base.
-- ---------------------------------------------------------------------

UPDATE proposition_condition pc
SET valeur = tc.id::text
FROM point_condition pt,
     type_contrat tc
WHERE pt.id = pc.id_point_condition
  AND pt.libelle = 'TYPE_CONTRAT'
  AND upper(trim(pc.valeur)) = upper(trim(tc.libelle));

COMMIT;

-- =====================================================================
-- FIN DU SCRIPT
-- =====================================================================