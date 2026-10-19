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


-- =====================================================================
-- BLOC 2 : RECRUTEMENT
-- =====================================================================

CREATE TABLE type_document (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE document (
    id               SERIAL PRIMARY KEY,
    id_type_document INT NOT NULL REFERENCES type_document(id),
    lien             VARCHAR(500) NOT NULL
);

CREATE TABLE type_contrat (
    id                   SERIAL PRIMARY KEY,
    libelle              VARCHAR(50) NOT NULL UNIQUE,   -- CDI, CDD, ...
    duree_max            INT NULL,                      -- en mois, NULL si illimitée
    renouvellement_max   INT NULL
);

CREATE TABLE offre_emploi (
    id                       SERIAL PRIMARY KEY,
    id_besoin                INT NOT NULL REFERENCES besoin(id),
    ref                      VARCHAR(50) NOT NULL UNIQUE,
    id_type_contrat          INT NOT NULL REFERENCES type_contrat(id),
    date_publication         DATE NULL,
    date_limite_candidature  DATE NULL,
    id_document              INT NULL REFERENCES document(id),
    etat                     VARCHAR(20) NOT NULL DEFAULT 'BROUILLON'
                             CHECK (etat IN ('BROUILLON', 'PUBLIEE', 'CLOTUREE'))
);

CREATE TABLE candidat (
    id             SERIAL PRIMARY KEY,
    nom            VARCHAR(100) NOT NULL,
    prenom         VARCHAR(150),
    date_naissance DATE NULL,
    adresse        VARCHAR(255),
    email          VARCHAR(150),
    numero         VARCHAR(30),
    id_document    INT NULL REFERENCES document(id)    -- CV principal
);

CREATE TABLE candidatures (
    id              SERIAL PRIMARY KEY,
    id_candidat     INT NOT NULL REFERENCES candidat(id),
    id_offre_emploi INT NOT NULL REFERENCES offre_emploi(id),
    date_depot      DATE NOT NULL DEFAULT CURRENT_DATE,
    etat            VARCHAR(20) NOT NULL DEFAULT 'DEPOSEE'
                    CHECK (etat IN ('DEPOSEE', 'PRESELECTIONNER', 'REFUSE', 'DESISTE')),
    UNIQUE (id_candidat, id_offre_emploi)
);

ALTER TABLE besoin_poste
    ADD CONSTRAINT fk_besoin_poste_candidature
    FOREIGN KEY (id_candidature_retenue) REFERENCES candidatures(id);

CREATE TABLE document_candidat (
    id             SERIAL PRIMARY KEY,
    id_candidature INT NOT NULL REFERENCES candidatures(id),
    id_document    INT NOT NULL REFERENCES document(id)
);

CREATE TABLE type_information (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(30) NOT NULL UNIQUE
            CHECK (libelle IN ('EXPERIENCE PRO', 'EDUCATION', 'QUALITE'))
);

CREATE TABLE candidat_detail (
    id                   SERIAL PRIMARY KEY,
    id_candidat          INT NOT NULL REFERENCES candidat(id),
    id_type_information  INT NOT NULL REFERENCES type_information(id),
    libelle              VARCHAR(255) NOT NULL,
    date_debut           DATE NULL,
    date_fin             DATE NULL
);

CREATE TABLE workflow_candidature (
    id               SERIAL PRIMARY KEY,
    ordre            INT NOT NULL UNIQUE,
    type_evaluation  VARCHAR(100) NOT NULL,
    score_min        NUMERIC(5,2) NULL
);

CREATE TABLE traitement (
    id             SERIAL PRIMARY KEY,
    id_candidature INT NOT NULL REFERENCES candidatures(id)
);

CREATE TABLE traitement_detail (
    id                              SERIAL PRIMARY KEY,
    id_traitement                   INT NOT NULL REFERENCES traitement(id),
    id_workflow_traitement          INT NOT NULL REFERENCES workflow_candidature(id),
    date_prevue                     DATE NULL,
    date_realisee                   DATE NULL,
    libelle                         VARCHAR(255),
    id_poste_responsable            INT NULL REFERENCES poste(id),
    id_user                         INT NULL REFERENCES "user"(id),
    score                           NUMERIC(5,2) NULL
);


-- =====================================================================
-- BLOC 3 : SÉLECTION ET EMBAUCHE
-- =====================================================================

CREATE TABLE point_condition (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(30) NOT NULL UNIQUE
            CHECK (libelle IN ('SALAIRE', 'DATE_DEBUT', 'HORAIRE', 'LIEU',
                               'CONDITIONS', 'TYPE_CONTRAT'))
);

CREATE TABLE proposition_embauche (
    id                    SERIAL PRIMARY KEY,
    id_candidature        INT NOT NULL REFERENCES candidatures(id),
    id_statut_actuel      VARCHAR(20) NOT NULL DEFAULT 'PROPOSER'
                          CHECK (id_statut_actuel IN ('PROPOSER', 'REFUSER', 'ACCEPTER')),
    id_besoin_poste       INT NOT NULL REFERENCES besoin_poste(id),
    date_limite_reponse   DATE NULL
);

CREATE TABLE proposition_condition (
    id                 SERIAL PRIMARY KEY,
    id_proposition     INT NOT NULL REFERENCES proposition_embauche(id),
    id_point_condition INT NOT NULL REFERENCES point_condition(id),
    valeur             TEXT NULL,
    accepte            BOOLEAN NULL,
    UNIQUE (id_proposition, id_point_condition)
);

CREATE TABLE relance_condition (
    id                        SERIAL PRIMARY KEY,
    id_proposition_condition  INT NOT NULL REFERENCES proposition_condition(id),
    nouvelle_valeur           TEXT NULL,
    statut                    VARCHAR(30) NULL
);

CREATE TABLE histo_proposition_embauche (
    id                        SERIAL PRIMARY KEY,
    id_proposition_embauche   INT NOT NULL REFERENCES proposition_embauche(id),
    id_statut                 VARCHAR(20) NOT NULL
                              CHECK (id_statut IN ('PROPOSER', 'REFUSER', 'ACCEPTER')),
    date                      TIMESTAMP NOT NULL DEFAULT NOW()
);


-- =====================================================================
-- BLOC 4 : CONTRAT ET ONBOARDING
-- =====================================================================

CREATE TABLE contrat (
    id                   SERIAL PRIMARY KEY,
    reference            VARCHAR(50) NOT NULL UNIQUE,
    id_affectation       INT NOT NULL REFERENCES affectation(id),
    id_proposition       INT NULL REFERENCES proposition_embauche(id),
    id_type              INT NOT NULL REFERENCES type_contrat(id),
    date_debut           DATE NOT NULL,
    date_fin_prevue      DATE NULL,
    date_fin_effective   DATE NULL,   -- NULL pour CDI, mise à jour en cas de rupture
    statut_actuel        VARCHAR(30) NOT NULL,
    id_contrat_precedent INT NULL REFERENCES contrat(id),
    id_document          INT NULL REFERENCES document(id)
);

CREATE TABLE contrat_details (
    id                   SERIAL PRIMARY KEY,
    id_contrat           INT NOT NULL REFERENCES contrat(id),
    id_point_condition   INT NOT NULL REFERENCES point_condition(id),
    valeur               TEXT NULL
);

CREATE TABLE contrat_remuneration (
    id          SERIAL PRIMARY KEY,
    id_contrat  INT NOT NULL REFERENCES contrat(id),
    date_effet  DATE NOT NULL,
    salaire     NUMERIC(15,2) NOT NULL,
    id_echelon  INT NULL REFERENCES echelon_metier(id)
);

CREATE TABLE etat_contrat (
    id         SERIAL PRIMARY KEY,
    id_contrat INT NOT NULL REFERENCES contrat(id),
    statut     VARCHAR(30) NOT NULL,
    date       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE workflow_onboarding (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(150) NOT NULL,
    statut  VARCHAR(30) NULL
);

CREATE TABLE workflow_onboarding_details (
    id                    SERIAL PRIMARY KEY,
    id_workflow           INT NOT NULL REFERENCES workflow_onboarding(id),
    ordre                 INT NOT NULL,
    type_tache            VARCHAR(100) NOT NULL,
    id_poste_responsable  INT NULL REFERENCES poste(id),
    delai_jours           INT NULL,
    obligatoire           BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (id_workflow, ordre)
);

CREATE TABLE onboarding (
    id          SERIAL PRIMARY KEY,
    id_contrat  INT NOT NULL REFERENCES contrat(id),
    id_workflow INT NOT NULL REFERENCES workflow_onboarding(id),
    statut      VARCHAR(30) NOT NULL,
    date_debut  DATE NULL,
    date_fin    DATE NULL
);

CREATE TABLE onboarding_details (
    id                    SERIAL PRIMARY KEY,
    id_onboarding         INT NOT NULL REFERENCES onboarding(id),
    id_workflow_details   INT NOT NULL REFERENCES workflow_onboarding_details(id),
    ordre                 INT NOT NULL,
    libelle               VARCHAR(255),
    type_tache            VARCHAR(100),
    id_poste_responsable  INT NULL REFERENCES poste(id),
    statut                VARCHAR(30) NOT NULL,
    id_user_realisateur   INT NULL REFERENCES "user"(id),
    date_realisation      DATE NULL,
    commentaire           TEXT NULL
);

-- =====================================================================
-- INDEX UTILES
-- =====================================================================
CREATE INDEX idx_unite_mere        ON unite_orga(id_unite_mere);
CREATE INDEX idx_poste_unite       ON poste(id_unite_orga);
CREATE INDEX idx_affectation_poste ON affectation(id_poste);
CREATE INDEX idx_besoin_unite      ON besoin(id_unite_orga);
CREATE INDEX idx_candidature_offre ON candidatures(id_offre_emploi);
CREATE INDEX idx_contrat_affect    ON contrat(id_affectation);
