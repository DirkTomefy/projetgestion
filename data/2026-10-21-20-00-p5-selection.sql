-- 1. Nouvel état RETENUE : fin de la sélection, la proposition d'embauche (P4) peut commencer
ALTER TABLE candidatures DROP CONSTRAINT IF EXISTS candidatures_etat_check;
ALTER TABLE candidatures ADD CONSTRAINT candidatures_etat_check
    CHECK (etat IN ('DEPOSEE', 'PRESELECTIONNER', 'REFUSE', 'DESISTE', 'RETENUE'));

-- 2. Historique des changements d'état d'une candidature (qui, quand, avant, après, pourquoi)
CREATE TABLE IF NOT EXISTS histo_candidature (
    id              SERIAL PRIMARY KEY,
    id_candidature  INT          NOT NULL REFERENCES candidatures(id),
    etat_avant      VARCHAR(20)  NULL,            -- NULL au dépôt
    etat_apres      VARCHAR(20)  NOT NULL,
    id_user         INT          NULL REFERENCES "user"(id),  -- NULL : dépôt public ou décision automatique
    date_changement TIMESTAMP    NOT NULL DEFAULT now(),
    commentaire     TEXT         NULL
);
CREATE INDEX IF NOT EXISTS idx_histo_candidature ON histo_candidature (id_candidature, date_changement);

-- Les candidatures déjà déposées reçoivent leur ligne de dépôt
INSERT INTO histo_candidature (id_candidature, etat_avant, etat_apres, date_changement, commentaire)
SELECT c.id, NULL, 'DEPOSEE', c.date_depot, 'Dépôt de la candidature'
FROM candidatures c
WHERE NOT EXISTS (SELECT 1 FROM histo_candidature h WHERE h.id_candidature = c.id);

-- 3. Niveaux de sélection : communs (id_offre_emploi NULL) ou propres à une offre
--    Une offre qui a ses propres niveaux n'utilise pas les niveaux communs.
ALTER TABLE workflow_candidature ADD COLUMN IF NOT EXISTS id_offre_emploi INT NULL REFERENCES offre_emploi(id);
ALTER TABLE workflow_candidature DROP CONSTRAINT IF EXISTS workflow_candidature_ordre_key;
CREATE UNIQUE INDEX IF NOT EXISTS ux_workflow_candidature_ordre
    ON workflow_candidature (COALESCE(id_offre_emploi, 0), ordre);

ALTER TABLE workflow_candidature DROP CONSTRAINT IF EXISTS ck_workflow_candidature_type;
ALTER TABLE workflow_candidature ADD CONSTRAINT ck_workflow_candidature_type
    CHECK (type_evaluation IN ('TEST', 'ENTRETIEN_RH', 'ENTRETIEN_CHEF_DEPARTEMENT', 'ENTRETIEN_DIRECTION'));
ALTER TABLE workflow_candidature DROP CONSTRAINT IF EXISTS ck_workflow_candidature_score;
ALTER TABLE workflow_candidature ADD CONSTRAINT ck_workflow_candidature_score
    CHECK (score_min IS NULL OR score_min BETWEEN 0 AND 20);
ALTER TABLE workflow_candidature DROP CONSTRAINT IF EXISTS ck_workflow_candidature_ordre;
ALTER TABLE workflow_candidature ADD CONSTRAINT ck_workflow_candidature_ordre CHECK (ordre > 0);

-- Niveaux communs par défaut (notes sur 20). Sans score minimum, l'évaluateur décide lui-même.
INSERT INTO workflow_candidature (ordre, type_evaluation, score_min)
SELECT v.ordre, v.type_evaluation, v.score_min
FROM (VALUES (1, 'TEST', 10.00),
             (2, 'ENTRETIEN_RH', 12.00),
             (3, 'ENTRETIEN_CHEF_DEPARTEMENT', 12.00),
             (4, 'ENTRETIEN_DIRECTION', NULL::NUMERIC)) AS v(ordre, type_evaluation, score_min)
WHERE NOT EXISTS (SELECT 1 FROM workflow_candidature w WHERE w.id_offre_emploi IS NULL);

-- 4. Un seul traitement par candidature, une seule évaluation par niveau
CREATE UNIQUE INDEX IF NOT EXISTS ux_traitement_candidature ON traitement (id_candidature);
CREATE UNIQUE INDEX IF NOT EXISTS ux_traitement_detail_niveau
    ON traitement_detail (id_traitement, id_workflow_traitement);

-- 5. Résultat d'une évaluation : décision et commentaire (un score seul ne dit pas qui passe)
ALTER TABLE traitement_detail ADD COLUMN IF NOT EXISTS decision    VARCHAR(20) NULL;
ALTER TABLE traitement_detail ADD COLUMN IF NOT EXISTS commentaire TEXT        NULL;
ALTER TABLE traitement_detail DROP CONSTRAINT IF EXISTS ck_traitement_detail_decision;
ALTER TABLE traitement_detail ADD CONSTRAINT ck_traitement_detail_decision
    CHECK (decision IS NULL OR decision IN ('ADMIS', 'NON_ADMIS', 'ABSENT'));
ALTER TABLE traitement_detail DROP CONSTRAINT IF EXISTS ck_traitement_detail_realisee;
ALTER TABLE traitement_detail ADD CONSTRAINT ck_traitement_detail_realisee
    CHECK (decision IS NULL OR date_realisee IS NOT NULL);
ALTER TABLE traitement_detail DROP CONSTRAINT IF EXISTS ck_traitement_detail_score;
ALTER TABLE traitement_detail ADD CONSTRAINT ck_traitement_detail_score
    CHECK (score IS NULL OR score BETWEEN 0 AND 20);
