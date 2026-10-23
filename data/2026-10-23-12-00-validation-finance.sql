-- =====================================================================
-- Budgets et grilles salariales validés par le DG.
-- La Finance propose, le DG valide ou refuse ; seules les valeurs validées servent
-- (contrôle budgétaire des besoins, fourchette des propositions d'embauche).
-- Les données existantes sont considérées comme validées. Peut être relancé sans erreur.
-- =====================================================================

-- ---------------------------------------------------------------- budgets
-- montant = montant validé (NULL tant qu'aucun montant n'a été validé)
-- montant_propose = montant en attente de la décision du DG
ALTER TABLE budget_unite ALTER COLUMN montant DROP NOT NULL;
ALTER TABLE budget_unite ADD COLUMN IF NOT EXISTS montant_propose  NUMERIC(18,2) NULL;
ALTER TABLE budget_unite ADD COLUMN IF NOT EXISTS statut           VARCHAR(20)   NOT NULL DEFAULT 'VALIDE';
ALTER TABLE budget_unite ADD COLUMN IF NOT EXISTS id_user_propose  INT           NULL REFERENCES "user"(id);
ALTER TABLE budget_unite ADD COLUMN IF NOT EXISTS id_user_decision INT           NULL REFERENCES "user"(id);
ALTER TABLE budget_unite ADD COLUMN IF NOT EXISTS date_decision    TIMESTAMP     NULL;
ALTER TABLE budget_unite ADD COLUMN IF NOT EXISTS commentaire      TEXT          NULL;
ALTER TABLE budget_unite DROP CONSTRAINT IF EXISTS ck_budget_unite_statut;
ALTER TABLE budget_unite ADD CONSTRAINT ck_budget_unite_statut CHECK (statut IN ('A_VALIDER', 'VALIDE', 'REFUSE'));

-- ---------------------------------------------------------------- grilles salariales
ALTER TABLE grille_salariale ADD COLUMN IF NOT EXISTS statut           VARCHAR(20) NOT NULL DEFAULT 'VALIDEE';
ALTER TABLE grille_salariale ADD COLUMN IF NOT EXISTS id_user_propose  INT         NULL REFERENCES "user"(id);
ALTER TABLE grille_salariale ADD COLUMN IF NOT EXISTS id_user_decision INT         NULL REFERENCES "user"(id);
ALTER TABLE grille_salariale ADD COLUMN IF NOT EXISTS date_decision    TIMESTAMP   NULL;
ALTER TABLE grille_salariale ADD COLUMN IF NOT EXISTS commentaire      TEXT        NULL;
ALTER TABLE grille_salariale DROP CONSTRAINT IF EXISTS ck_grille_salariale_statut;
ALTER TABLE grille_salariale ADD CONSTRAINT ck_grille_salariale_statut CHECK (statut IN ('A_VALIDER', 'VALIDEE', 'REFUSEE'));
