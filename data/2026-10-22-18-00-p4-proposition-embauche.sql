-- =====================================================================
-- P4 – Sprint 2 : proposition d'embauche et clôture des offres (blocs 2 et 3)
-- Colonnes et contraintes attendues par les entités de model/embauche et OffreEmploi.
-- Peut être relancé sans erreur.
-- =====================================================================

-- 1. Points de condition d'une proposition (référentiel)
INSERT INTO point_condition (libelle)
VALUES ('SALAIRE'), ('DATE_DEBUT'), ('HORAIRE'), ('LIEU'), ('CONDITIONS'), ('TYPE_CONTRAT')
ON CONFLICT (libelle) DO NOTHING;

-- 2. Proposition : lien personnel du candidat (jeton), dates d'envoi et de réponse, statut EXPIREE
ALTER TABLE proposition_embauche ADD COLUMN IF NOT EXISTS jeton        VARCHAR(64) NULL;
ALTER TABLE proposition_embauche ADD COLUMN IF NOT EXISTS date_envoi   TIMESTAMP   NULL;
ALTER TABLE proposition_embauche ADD COLUMN IF NOT EXISTS date_reponse TIMESTAMP   NULL;
UPDATE proposition_embauche SET jeton = md5(random()::text || id::text) || md5(clock_timestamp()::text)
WHERE jeton IS NULL;
ALTER TABLE proposition_embauche ALTER COLUMN jeton SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_proposition_embauche_jeton ON proposition_embauche (jeton);
ALTER TABLE proposition_embauche DROP CONSTRAINT IF EXISTS proposition_embauche_id_statut_actuel_check;
ALTER TABLE proposition_embauche ADD CONSTRAINT proposition_embauche_id_statut_actuel_check
    CHECK (id_statut_actuel IN ('PROPOSER', 'REFUSER', 'ACCEPTER', 'EXPIREE'));

-- 3. Historique des statuts : qui et pourquoi
ALTER TABLE histo_proposition_embauche ADD COLUMN IF NOT EXISTS id_user     INT  NULL REFERENCES "user"(id);
ALTER TABLE histo_proposition_embauche ADD COLUMN IF NOT EXISTS commentaire TEXT NULL;
ALTER TABLE histo_proposition_embauche DROP CONSTRAINT IF EXISTS histo_proposition_embauche_id_statut_check;
ALTER TABLE histo_proposition_embauche ADD CONSTRAINT histo_proposition_embauche_id_statut_check
    CHECK (id_statut IN ('PROPOSER', 'REFUSER', 'ACCEPTER', 'EXPIREE'));

-- 4. Réponse du candidat condition par condition
ALTER TABLE proposition_condition ADD COLUMN IF NOT EXISTS commentaire_candidat TEXT NULL;

-- 5. Contre-propositions (relances) : ancienne valeur, auteur, date, commentaire
ALTER TABLE relance_condition ADD COLUMN IF NOT EXISTS ancienne_valeur TEXT      NULL;
ALTER TABLE relance_condition ADD COLUMN IF NOT EXISTS id_user         INT       NULL REFERENCES "user"(id);
ALTER TABLE relance_condition ADD COLUMN IF NOT EXISTS date_relance    TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE relance_condition ADD COLUMN IF NOT EXISTS commentaire     TEXT      NULL;
ALTER TABLE relance_condition DROP CONSTRAINT IF EXISTS ck_relance_condition_statut;
ALTER TABLE relance_condition ADD CONSTRAINT ck_relance_condition_statut
    CHECK (statut IS NULL OR statut IN ('EN_ATTENTE', 'ACCEPTEE', 'REFUSEE'));

-- 6. Clôture des offres : quand, pourquoi, par qui (NULL = tâche automatique)
ALTER TABLE offre_emploi ADD COLUMN IF NOT EXISTS date_cloture    TIMESTAMP   NULL;
ALTER TABLE offre_emploi ADD COLUMN IF NOT EXISTS motif_cloture   VARCHAR(20) NULL;
ALTER TABLE offre_emploi ADD COLUMN IF NOT EXISTS id_user_cloture INT         NULL REFERENCES "user"(id);
ALTER TABLE offre_emploi DROP CONSTRAINT IF EXISTS ck_offre_emploi_motif_cloture;
ALTER TABLE offre_emploi ADD CONSTRAINT ck_offre_emploi_motif_cloture
    CHECK (motif_cloture IS NULL OR motif_cloture IN ('DATE_LIMITE', 'POSTES_POURVUS', 'MANUELLE'));
