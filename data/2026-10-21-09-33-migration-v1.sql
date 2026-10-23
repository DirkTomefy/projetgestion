-- =====================================================================
-- Migration : delegation, personne, candidat (PostgreSQL)
-- =====================================================================

-- 1. Nouvelle table personne
CREATE TABLE personne (
    id             SERIAL PRIMARY KEY,
    nom            VARCHAR(100) NOT NULL,
    prenoms        VARCHAR(150),
    adresse        VARCHAR(255),
    telephone      VARCHAR(30),
    date_naissance DATE NULL,
    cin            VARCHAR(30) NULL UNIQUE,
    email          VARCHAR(150)
);

-- 2. Nouvelle table delegation
CREATE TABLE delegation (
    id               SERIAL PRIMARY KEY,
    id_user_delegant INT  NOT NULL REFERENCES "user"(id),
    id_user_delegue  INT  NOT NULL REFERENCES "user"(id),
    date_debut       DATE NOT NULL,
    date_fin         DATE NOT NULL,
    CHECK (id_user_delegant <> id_user_delegue),
    CHECK (date_fin >= date_debut)
);

-- 3. Modification de candidat
ALTER TABLE candidat
    ADD COLUMN id_personne INT REFERENCES personne(id);

ALTER TABLE candidat
    DROP COLUMN id_document;

-- À exécuter une fois que id_personne est renseigné pour tous les candidats existants
-- (si la table candidat est vide, tu peux l'exécuter tout de suite)
ALTER TABLE candidat
    ALTER COLUMN id_personne SET NOT NULL;


CREATE TABLE notification (
    id            SERIAL PRIMARY KEY,
    id_utilisateur INT          NOT NULL REFERENCES "user"(id),
    type          VARCHAR(50)  NOT NULL,
    message       TEXT         NOT NULL,
    lien          VARCHAR(500) NULL,
    lu            BOOLEAN      NOT NULL DEFAULT FALSE,
    date_creation TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notification_user_lu ON notification(id_utilisateur, lu);