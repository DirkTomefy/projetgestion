-- =====================================================================
-- Schéma RH : organisation, besoin, recrutement, embauche, contrat
-- SGBD : PostgreSQL
-- =====================================================================

-- =====================================================================
-- STRUCTURE ORGANISATIONNELLE
-- =====================================================================

CREATE TABLE unite_orga (
    id                    SERIAL PRIMARY KEY,
    code                  VARCHAR(50)  NOT NULL UNIQUE,
    type                  VARCHAR(20)  NOT NULL
                          CHECK (type IN ('DIRECTION', 'DEPARTEMENT', 'SERVICE', 'SECTION')),
    libelle               VARCHAR(150) NOT NULL,
    id_unite_mere         INT NULL REFERENCES unite_orga(id),
    id_poste_responsable  INT NULL      -- FK vers poste ajoutée plus bas
);

CREATE TABLE categorie_metier (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE echelon_metier (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE metier (
    id           SERIAL PRIMARY KEY,
    libelle      VARCHAR(150) NOT NULL,
    id_categorie INT NOT NULL REFERENCES categorie_metier(id)
);

CREATE TABLE poste (
    id                   SERIAL PRIMARY KEY,
    code                 VARCHAR(50)  NOT NULL UNIQUE,
    id_metier            INT NOT NULL REFERENCES metier(id),
    id_echelon           INT NOT NULL REFERENCES echelon_metier(id),
    id_unite_orga        INT NOT NULL REFERENCES unite_orga(id),
    id_superieur_direct  INT NULL REFERENCES poste(id),
    libelle              VARCHAR(150) NOT NULL,
    etat                 VARCHAR(20) NOT NULL DEFAULT 'PROPOSE'
                         CHECK (etat IN ('PROPOSE', 'ACTIF', 'REFUSE', 'SUPPRIME')),
    statut_occupation    VARCHAR(20) NOT NULL DEFAULT 'LIBRE'
                         CHECK (statut_occupation IN ('LIBRE', 'OCCUPE'))
);

ALTER TABLE unite_orga
    ADD CONSTRAINT fk_unite_orga_poste_resp
    FOREIGN KEY (id_poste_responsable) REFERENCES poste(id);

CREATE TABLE employe (
    id        SERIAL PRIMARY KEY,
    matricule VARCHAR(30)  NOT NULL UNIQUE,
    nom       VARCHAR(100) NOT NULL,
    prenoms   VARCHAR(150),
    adresse   VARCHAR(255)
);

CREATE TABLE affectation (
    id         SERIAL PRIMARY KEY,
    id_poste   INT  NOT NULL REFERENCES poste(id),
    id_employe INT  NOT NULL REFERENCES employe(id),
    date_debut DATE NOT NULL,           -- dès le premier contrat
    date_fin   DATE NULL,               -- NULL si contrat en cours
    CHECK (date_fin IS NULL OR date_fin >= date_debut)
);

-- =====================================================================
-- GRILLE SALARIALE & BUDGET
-- =====================================================================

CREATE TABLE grille_salariale (
    id             SERIAL PRIMARY KEY,
    date_effectif  DATE NOT NULL,
    date_fin       DATE NULL,
    CHECK (date_fin IS NULL OR date_fin >= date_effectif)
);

CREATE TABLE grille_salariale_details (
    id           SERIAL PRIMARY KEY,
    id_grille    INT NOT NULL REFERENCES grille_salariale(id),
    id_categorie INT NOT NULL REFERENCES categorie_metier(id),
    id_echelon   INT NOT NULL REFERENCES echelon_metier(id),
    montant_min  NUMERIC(15,2) NOT NULL,
    montant_max  NUMERIC(15,2) NOT NULL,
    CHECK (montant_max >= montant_min),
    UNIQUE (id_grille, id_categorie, id_echelon)
);

CREATE TABLE budget_unite (
    id            SERIAL PRIMARY KEY,
    id_unite_orga INT NOT NULL REFERENCES unite_orga(id),
    exercice      INT NOT NULL,
    montant       NUMERIC(15,2) NOT NULL,
    UNIQUE (id_unite_orga, exercice)
);

-- =====================================================================
-- UTILISATEURS & RÔLES
-- =====================================================================

CREATE TABLE "user" (
    id           SERIAL PRIMARY KEY,
    login        VARCHAR(100) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    id_employe   INT NULL REFERENCES employe(id)
);

CREATE TABLE role (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE user_role (
    id      SERIAL PRIMARY KEY,
    id_user INT NOT NULL REFERENCES "user"(id),
    id_role INT NOT NULL REFERENCES role(id),
    UNIQUE (id_user, id_role)
);

-- =====================================================================
-- BLOC 1 : EXPRESSION DU BESOIN
-- =====================================================================

CREATE TABLE motif (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(150) NOT NULL
);

CREATE TABLE besoin (
    id              SERIAL PRIMARY KEY,
    reference       VARCHAR(50) NOT NULL UNIQUE,
    type_besoin     VARCHAR(50) NOT NULL,
    id_motif        INT NOT NULL REFERENCES motif(id),
    id_unite_orga   INT NOT NULL REFERENCES unite_orga(id),
    date_souhaitee  DATE NULL,
    id_user         INT NOT NULL REFERENCES "user"(id),
    effectif        INT NOT NULL CHECK (effectif > 0),
    etat_actuel     VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE'
                    CHECK (etat_actuel IN ('EN_ATTENTE', 'OUVERT', 'POURVU', 'ANNULE')),
    budget_estime   NUMERIC(15,2) NULL,
    precision_motif VARCHAR(100) NULL
);

-- id_candidature_retenue : FK ajoutée après création de candidatures
CREATE TABLE besoin_poste (
    id                      SERIAL PRIMARY KEY,
    id_besoin               INT NOT NULL REFERENCES besoin(id),
    id_poste                INT NOT NULL REFERENCES poste(id),
    etat                    VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE'
                            CHECK (etat IN ('EN_ATTENTE', 'OUVERT', 'POURVU', 'ANNULE')),
    id_candidature_retenue  INT NULL
);

CREATE TABLE workflow_validation (
    id       SERIAL PRIMARY KEY,
    id_poste INT NOT NULL REFERENCES poste(id),   -- poste validateur
    ordre    INT NOT NULL,
    UNIQUE (id_poste, ordre)
);

CREATE TABLE validation (
    id                      SERIAL PRIMARY KEY,
    id_user                 INT NOT NULL REFERENCES "user"(id),
    id_besoin               INT NOT NULL REFERENCES besoin(id),
    id_workflow_validation  INT NOT NULL REFERENCES workflow_validation(id),
    decision                VARCHAR(30) NOT NULL,
    date                    TIMESTAMP NOT NULL DEFAULT NOW(),
    commentaire             TEXT NULL
);

