-- =====================================================================
-- P4 (Fifaliana) - Sprint 2 : proposition d'embauche et clôture des offres
-- À exécuter après 2026-10-21-20-00-p5-selection.sql. Rejouable sans erreur.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Points de condition d'une proposition (codes lus aussi par l'embauche P3)
-- ---------------------------------------------------------------------
INSERT INTO point_condition (libelle)
VALUES ('SALAIRE'), ('DATE_DEBUT'), ('HORAIRE'), ('LIEU'), ('CONDITIONS'), ('TYPE_CONTRAT')
ON CONFLICT (libelle) DO NOTHING;

-- ---------------------------------------------------------------------
-- 2. proposition_embauche : lien de réponse du candidat (décision 5) et délais
--    jeton       : aléatoire, unique, envoyé au candidat dans son lien personnel
--    date_envoi  : envoi de la proposition (ou de la dernière contre-proposition)
--    date_reponse: dernière réponse du candidat
--    EXPIREE     : date limite dépassée sans réponse
-- ---------------------------------------------------------------------
ALTER TABLE proposition_embauche ADD COLUMN IF NOT EXISTS jeton        VARCHAR(64) NULL;
ALTER TABLE proposition_embauche ADD COLUMN IF NOT EXISTS date_envoi   TIMESTAMP   NULL;
ALTER TABLE proposition_embauche ADD COLUMN IF NOT EXISTS date_reponse TIMESTAMP   NULL;

-- Propositions déjà saisies à la main : on leur donne un jeton
UPDATE proposition_embauche
   SET jeton = md5(random()::text || clock_timestamp()::text || id::text)
 WHERE jeton IS NULL;
ALTER TABLE proposition_embauche ALTER COLUMN jeton SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_proposition_embauche_jeton ON proposition_embauche (jeton);

ALTER TABLE proposition_embauche DROP CONSTRAINT IF EXISTS proposition_embauche_id_statut_actuel_check;
ALTER TABLE proposition_embauche ADD CONSTRAINT proposition_embauche_id_statut_actuel_check
    CHECK (id_statut_actuel IN ('PROPOSER', 'REFUSER', 'ACCEPTER', 'EXPIREE'));

-- Un siège ne reçoit qu'une proposition en cours ou acceptée à la fois
CREATE UNIQUE INDEX IF NOT EXISTS ux_proposition_siege_en_cours
    ON proposition_embauche (id_besoin_poste)
    WHERE id_statut_actuel IN ('PROPOSER', 'ACCEPTER');
-- Une candidature ne reçoit qu'une proposition en cours ou acceptée à la fois
CREATE UNIQUE INDEX IF NOT EXISTS ux_proposition_candidature_en_cours
    ON proposition_embauche (id_candidature)
    WHERE id_statut_actuel IN ('PROPOSER', 'ACCEPTER');

-- ---------------------------------------------------------------------
-- 3. proposition_condition : motif donné par le candidat quand il refuse
-- ---------------------------------------------------------------------
ALTER TABLE proposition_condition ADD COLUMN IF NOT EXISTS commentaire_candidat TEXT NULL;

-- ---------------------------------------------------------------------
-- 4. relance_condition : contre-proposition sur une condition refusée
--    On garde l'ancienne valeur : l'historique des conditions n'est jamais écrasé.
--    statut : EN_ATTENTE (envoyée au candidat), ACCEPTEE, REFUSEE
-- ---------------------------------------------------------------------
ALTER TABLE relance_condition ADD COLUMN IF NOT EXISTS ancienne_valeur TEXT      NULL;
ALTER TABLE relance_condition ADD COLUMN IF NOT EXISTS id_user         INT       NULL REFERENCES "user"(id);
ALTER TABLE relance_condition ADD COLUMN IF NOT EXISTS date_relance    TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE relance_condition ADD COLUMN IF NOT EXISTS commentaire     TEXT      NULL;
UPDATE relance_condition SET statut = 'EN_ATTENTE' WHERE statut IS NULL;
ALTER TABLE relance_condition DROP CONSTRAINT IF EXISTS ck_relance_condition_statut;
ALTER TABLE relance_condition ADD CONSTRAINT ck_relance_condition_statut
    CHECK (statut IN ('EN_ATTENTE', 'ACCEPTEE', 'REFUSEE'));
CREATE INDEX IF NOT EXISTS idx_relance_condition ON relance_condition (id_proposition_condition);

-- ---------------------------------------------------------------------
-- 5. histo_proposition_embauche : qui, quand, pourquoi
--    id_user NULL : action du candidat (page publique) ou tâche automatique
--    Une ligne par événement ; id_statut = statut de la proposition après l'événement.
-- ---------------------------------------------------------------------
ALTER TABLE histo_proposition_embauche ADD COLUMN IF NOT EXISTS id_user     INT  NULL REFERENCES "user"(id);
ALTER TABLE histo_proposition_embauche ADD COLUMN IF NOT EXISTS commentaire TEXT NULL;
ALTER TABLE histo_proposition_embauche DROP CONSTRAINT IF EXISTS histo_proposition_embauche_id_statut_check;
ALTER TABLE histo_proposition_embauche ADD CONSTRAINT histo_proposition_embauche_id_statut_check
    CHECK (id_statut IN ('PROPOSER', 'REFUSER', 'ACCEPTER', 'EXPIREE'));
CREATE INDEX IF NOT EXISTS idx_histo_proposition ON histo_proposition_embauche (id_proposition_embauche, date);

-- ---------------------------------------------------------------------
-- 6. offre_emploi : traçabilité de la clôture (bloc 2)
--    motif_cloture : DATE_LIMITE (tâche quotidienne), POSTES_POURVUS, MANUELLE (bouton RH)
-- ---------------------------------------------------------------------
ALTER TABLE offre_emploi ADD COLUMN IF NOT EXISTS date_cloture    TIMESTAMP   NULL;
ALTER TABLE offre_emploi ADD COLUMN IF NOT EXISTS motif_cloture   VARCHAR(20) NULL;
ALTER TABLE offre_emploi ADD COLUMN IF NOT EXISTS id_user_cloture INT         NULL REFERENCES "user"(id);
ALTER TABLE offre_emploi DROP CONSTRAINT IF EXISTS ck_offre_emploi_motif_cloture;
ALTER TABLE offre_emploi ADD CONSTRAINT ck_offre_emploi_motif_cloture
    CHECK (motif_cloture IS NULL OR motif_cloture IN ('DATE_LIMITE', 'POSTES_POURVUS', 'MANUELLE'));

-- ---------------------------------------------------------------------
-- 7. TYPE_CONTRAT d'une proposition : stocké par id de type_contrat (lu ainsi par l'embauche P3).
--    Conversion des valeurs saisies par libellé (CE, CDD, CDI) avant cette règle.
-- ---------------------------------------------------------------------
UPDATE proposition_condition pc
   SET valeur = tc.id::text
  FROM point_condition pt, type_contrat tc
 WHERE pt.id = pc.id_point_condition
   AND pt.libelle = 'TYPE_CONTRAT'
   AND upper(trim(pc.valeur)) = upper(tc.libelle);
