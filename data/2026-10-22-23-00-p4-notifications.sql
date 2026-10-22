-- =====================================================================
-- P4 (Fifaliana) - Sprint 3 : notifications dans l'application
-- La table a été créée par 2026-10-21-09-33-migration-v1.sql ; ce script la crée si besoin
-- et ajoute ce qu'il faut pour la cloche. Rejouable sans erreur.
-- =====================================================================

CREATE TABLE IF NOT EXISTS notification (
    id             SERIAL PRIMARY KEY,
    id_utilisateur INT          NOT NULL REFERENCES "user"(id),   -- destinataire
    type           VARCHAR(50)  NOT NULL,                        -- VALIDATION_EN_ATTENTE, CANDIDATURE_RECUE, ...
    message        TEXT         NOT NULL,
    lien           VARCHAR(500) NULL,                            -- route du front à ouvrir
    lu             BOOLEAN      NOT NULL DEFAULT FALSE,
    date_creation  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Pastille de la cloche (non lues) et liste par date
CREATE INDEX IF NOT EXISTS idx_notification_user_lu   ON notification (id_utilisateur, lu);
CREATE INDEX IF NOT EXISTS idx_notification_user_date ON notification (id_utilisateur, date_creation DESC);
-- Éviter de notifier deux fois le même événement (ex. contrat à échéance, contrôle quotidien)
CREATE INDEX IF NOT EXISTS idx_notification_type_lien ON notification (type, lien);

ALTER TABLE notification DROP CONSTRAINT IF EXISTS ck_notification_type;
ALTER TABLE notification ADD CONSTRAINT ck_notification_type CHECK (type ~ '^[A-Z][A-Z0-9_]*$');
