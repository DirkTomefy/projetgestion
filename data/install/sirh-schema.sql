-- =====================================================================
-- SIRH – structure complète de la base (tables, contraintes, index)
-- Un seul script, équivalent à tous les scripts de data/ rejoués dans l'ordre (blocs 0 à 4, sprint 3).
-- Ordre d'installation : 1) ce fichier  2) sirh-donnees.sql
-- Il n'y a pas de vue dans la base actuelle.
--
-- ATTENTION : la première instruction supprime tout le schéma public (réinstallation).
-- =====================================================================

DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
SET search_path = public;

-- ---------------------------------------------------------------------
-- Tables (52)
-- ---------------------------------------------------------------------

CREATE TABLE affectation (
    id SERIAL,
    id_poste integer NOT NULL,
    id_employe integer NOT NULL,
    date_debut date NOT NULL,
    date_fin date,
    CONSTRAINT affectation_check CHECK (((date_fin IS NULL) OR (date_fin >= date_debut)))
);

CREATE TABLE besoin (
    id SERIAL,
    reference VARCHAR(50) NOT NULL,
    type_besoin VARCHAR(50) NOT NULL,
    id_motif integer NOT NULL,
    id_unite_orga integer NOT NULL,
    date_souhaitee date,
    id_user integer NOT NULL,
    effectif integer NOT NULL,
    etat_actuel VARCHAR(20) DEFAULT 'EN_ATTENTE' NOT NULL,
    budget_estime numeric(15,2),
    precision_motif VARCHAR(100),
    CONSTRAINT besoin_effectif_check CHECK ((effectif > 0)),
    CONSTRAINT besoin_etat_actuel_check CHECK (((etat_actuel)::text = ANY ((ARRAY['EN_ATTENTE', 'OUVERT', 'POURVU', 'ANNULE'])::text[])))
);

CREATE TABLE besoin_poste (
    id SERIAL,
    id_besoin integer NOT NULL,
    id_poste integer NOT NULL,
    etat VARCHAR(20) DEFAULT 'EN_ATTENTE' NOT NULL,
    id_candidature_retenue integer,
    id_employe_remplace integer,
    CONSTRAINT besoin_poste_etat_check CHECK (((etat)::text = ANY ((ARRAY['EN_ATTENTE', 'OUVERT', 'POURVU', 'ANNULE'])::text[])))
);

CREATE TABLE budget_unite (
    id SERIAL,
    id_unite_orga integer NOT NULL,
    exercice integer NOT NULL,
    montant numeric(15,2),
    montant_propose numeric(18,2),
    statut VARCHAR(20) DEFAULT 'VALIDE' NOT NULL,
    id_user_propose integer,
    id_user_decision integer,
    date_decision TIMESTAMP,
    commentaire text,
    CONSTRAINT ck_budget_unite_statut CHECK (((statut)::text = ANY ((ARRAY['A_VALIDER', 'VALIDE', 'REFUSE'])::text[])))
);

CREATE TABLE candidat (
    id SERIAL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(150),
    date_naissance date,
    adresse VARCHAR(255),
    email VARCHAR(150),
    numero VARCHAR(30),
    id_personne integer NOT NULL
);

CREATE TABLE candidat_detail (
    id SERIAL,
    id_candidat integer NOT NULL,
    id_type_information integer NOT NULL,
    libelle VARCHAR(255) NOT NULL,
    date_debut date,
    date_fin date,
    CONSTRAINT ck_candidat_detail_dates CHECK (((date_fin IS NULL) OR (date_debut IS NULL) OR (date_fin >= date_debut)))
);

CREATE TABLE candidatures (
    id SERIAL,
    id_candidat integer NOT NULL,
    id_offre_emploi integer NOT NULL,
    date_depot date DEFAULT CURRENT_DATE NOT NULL,
    etat VARCHAR(20) DEFAULT 'DEPOSEE' NOT NULL,
    CONSTRAINT candidatures_etat_check CHECK (((etat)::text = ANY ((ARRAY['DEPOSEE', 'PRESELECTIONNER', 'REFUSE', 'DESISTE', 'RETENUE'])::text[])))
);

CREATE TABLE categorie_metier (
    id SERIAL,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE contrat (
    id SERIAL,
    reference VARCHAR(50) NOT NULL,
    id_affectation integer NOT NULL,
    id_proposition integer,
    id_type integer NOT NULL,
    date_debut date NOT NULL,
    date_fin_prevue date,
    date_fin_effective date,
    statut_actuel VARCHAR(30) NOT NULL,
    id_contrat_precedent integer,
    id_document integer,
    motif_fin VARCHAR(255),
    CONSTRAINT contrat_statut_actuel_check CHECK (((statut_actuel)::text = ANY ((ARRAY['CREE', 'VALIDE', 'SIGNE', 'TERMINE', 'ANNULE'])::text[])))
);

CREATE TABLE contrat_details (
    id SERIAL,
    id_contrat integer NOT NULL,
    id_point_condition integer NOT NULL,
    valeur text
);

CREATE TABLE contrat_remuneration (
    id SERIAL,
    id_contrat integer NOT NULL,
    date_effet date NOT NULL,
    salaire numeric(15,2) NOT NULL,
    id_echelon integer
);

CREATE TABLE coordonnees_bancaires (
    id SERIAL,
    id_employe integer NOT NULL,
    banque VARCHAR(150) NOT NULL,
    agence VARCHAR(150),
    titulaire VARCHAR(200) NOT NULL,
    rib VARCHAR(50) NOT NULL,
    date_effet date DEFAULT CURRENT_DATE NOT NULL,
    actif boolean DEFAULT true NOT NULL
);

CREATE TABLE delegation (
    id SERIAL,
    id_user_delegant integer NOT NULL,
    id_user_delegue integer NOT NULL,
    date_debut date NOT NULL,
    date_fin date NOT NULL,
    CONSTRAINT delegation_check CHECK ((id_user_delegant <> id_user_delegue)),
    CONSTRAINT delegation_check1 CHECK ((date_fin >= date_debut))
);

CREATE TABLE demande_contrat (
    id SERIAL,
    reference VARCHAR(30) NOT NULL,
    id_contrat integer NOT NULL,
    origine VARCHAR(10) NOT NULL,
    motif_type VARCHAR(20) NOT NULL,
    id_type_contrat integer,
    date_effet date NOT NULL,
    date_fin_prevue date,
    salaire numeric(15,2) NOT NULL,
    motif text,
    etat VARCHAR(20) NOT NULL,
    depassement_budget boolean DEFAULT false NOT NULL,
    id_demandeur integer NOT NULL,
    date_demande TIMESTAMP DEFAULT now() NOT NULL,
    id_rh integer,
    date_rh TIMESTAMP,
    commentaire_rh text,
    id_finance integer,
    date_finance TIMESTAMP,
    commentaire_finance text,
    id_dg integer,
    date_dg TIMESTAMP,
    commentaire_dg text,
    id_contrat_resultat integer
);

CREATE TABLE document (
    id SERIAL,
    id_type_document integer NOT NULL,
    lien VARCHAR(500) NOT NULL
);

CREATE TABLE document_candidat (
    id SERIAL,
    id_candidature integer NOT NULL,
    id_document integer NOT NULL
);

CREATE TABLE echelon_metier (
    id SERIAL,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE employe (
    id SERIAL,
    matricule VARCHAR(30) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenoms VARCHAR(150),
    adresse VARCHAR(255),
    id_personne integer
);

CREATE TABLE etat_contrat (
    id SERIAL,
    id_contrat integer NOT NULL,
    statut VARCHAR(30) NOT NULL,
    date TIMESTAMP DEFAULT now() NOT NULL,
    CONSTRAINT etat_contrat_statut_check CHECK (((statut)::text = ANY ((ARRAY['CREE', 'VALIDE', 'SIGNE', 'TERMINE', 'ANNULE'])::text[])))
);

CREATE TABLE fiche_poste (
    id SERIAL,
    id_metier integer,
    id_poste integer,
    mission text NOT NULL,
    activites text,
    formation text,
    experience text,
    competences text,
    conditions text,
    version integer DEFAULT 1 NOT NULL,
    date_maj TIMESTAMP DEFAULT now() NOT NULL,
    id_auteur integer,
    CONSTRAINT ck_fiche_poste_portee CHECK (((id_metier IS NOT NULL) <> (id_poste IS NOT NULL)))
);

CREATE TABLE grille_salariale (
    id SERIAL,
    date_effectif date NOT NULL,
    date_fin date,
    statut VARCHAR(20) DEFAULT 'VALIDEE' NOT NULL,
    id_user_propose integer,
    id_user_decision integer,
    date_decision TIMESTAMP,
    commentaire text,
    CONSTRAINT ck_grille_salariale_statut CHECK (((statut)::text = ANY ((ARRAY['A_VALIDER', 'VALIDEE', 'REFUSEE'])::text[]))),
    CONSTRAINT grille_salariale_check CHECK (((date_fin IS NULL) OR (date_fin >= date_effectif)))
);

CREATE TABLE grille_salariale_details (
    id SERIAL,
    id_grille integer NOT NULL,
    id_categorie integer NOT NULL,
    id_echelon integer NOT NULL,
    montant_min numeric(15,2) NOT NULL,
    montant_max numeric(15,2) NOT NULL,
    CONSTRAINT grille_salariale_details_check CHECK ((montant_max >= montant_min))
);

CREATE TABLE histo_besoin (
    id SERIAL,
    id_besoin integer NOT NULL,
    etat_avant VARCHAR(20),
    etat_apres VARCHAR(20) NOT NULL,
    action VARCHAR(50) NOT NULL,
    id_user integer,
    date_action TIMESTAMP DEFAULT now() NOT NULL,
    commentaire text
);

CREATE TABLE histo_candidature (
    id SERIAL,
    id_candidature integer NOT NULL,
    etat_avant VARCHAR(20),
    etat_apres VARCHAR(20) NOT NULL,
    id_user integer,
    date_changement TIMESTAMP DEFAULT now() NOT NULL,
    commentaire text
);

CREATE TABLE histo_proposition_embauche (
    id SERIAL,
    id_proposition_embauche integer NOT NULL,
    id_statut VARCHAR(20) NOT NULL,
    date TIMESTAMP DEFAULT now() NOT NULL,
    id_user integer,
    commentaire text,
    CONSTRAINT histo_proposition_embauche_id_statut_check CHECK (((id_statut)::text = ANY ((ARRAY['PROPOSER', 'REFUSER', 'ACCEPTER', 'EXPIREE'])::text[])))
);

CREATE TABLE journal_audit (
    id SERIAL,
    date_action TIMESTAMP DEFAULT now() NOT NULL,
    id_user integer,
    login VARCHAR(100),
    action VARCHAR(200) NOT NULL,
    methode VARCHAR(10) NOT NULL,
    chemin VARCHAR(300) NOT NULL,
    statut_http integer NOT NULL,
    adresse_ip VARCHAR(60)
);

CREATE TABLE metier (
    id SERIAL,
    libelle VARCHAR(150) NOT NULL,
    id_categorie integer NOT NULL
);

CREATE TABLE motif (
    id SERIAL,
    libelle VARCHAR(150) NOT NULL
);

CREATE TABLE notification (
    id SERIAL,
    id_utilisateur integer NOT NULL,
    type VARCHAR(50) NOT NULL,
    message text NOT NULL,
    lien VARCHAR(500),
    lu boolean DEFAULT false NOT NULL,
    date_creation TIMESTAMP DEFAULT now() NOT NULL,
    CONSTRAINT ck_notification_type CHECK (((type)::text ~ '^[A-Z][A-Z0-9_]*$'::text))
);

CREATE TABLE offre_emploi (
    id SERIAL,
    id_besoin integer NOT NULL,
    ref VARCHAR(50) NOT NULL,
    id_type_contrat integer,
    date_publication date,
    date_limite_candidature date,
    id_document integer,
    etat VARCHAR(20) DEFAULT 'BROUILLON' NOT NULL,
    date_cloture TIMESTAMP,
    motif_cloture VARCHAR(20),
    id_user_cloture integer,
    id_poste integer,
    id_fiche_poste integer,
    CONSTRAINT ck_offre_emploi_motif_cloture CHECK (((motif_cloture IS NULL) OR ((motif_cloture)::text = ANY ((ARRAY['DATE_LIMITE', 'POSTES_POURVUS', 'MANUELLE'])::text[])))),
    CONSTRAINT offre_emploi_etat_check CHECK (((etat)::text = ANY ((ARRAY['BROUILLON', 'PUBLIEE', 'CLOTUREE'])::text[])))
);

CREATE TABLE onboarding (
    id SERIAL,
    id_contrat integer NOT NULL,
    id_workflow integer NOT NULL,
    statut VARCHAR(30) NOT NULL,
    date_debut date,
    date_fin date,
    CONSTRAINT ck_onboarding_statut CHECK (((statut)::text = ANY ((ARRAY['EN_COURS', 'TERMINE'])::text[])))
);

CREATE TABLE onboarding_details (
    id SERIAL,
    id_onboarding integer NOT NULL,
    id_workflow_details integer NOT NULL,
    ordre integer NOT NULL,
    libelle VARCHAR(255),
    type_tache VARCHAR(100),
    id_poste_responsable integer,
    statut VARCHAR(30) NOT NULL,
    id_user_realisateur integer,
    date_realisation date,
    commentaire text,
    date_echeance date,
    CONSTRAINT ck_onboarding_details_statut CHECK (((statut)::text = ANY ((ARRAY['A_FAIRE', 'FAIT', 'NON_APPLICABLE'])::text[])))
);

CREATE TABLE personne (
    id SERIAL,
    nom VARCHAR(100) NOT NULL,
    prenoms VARCHAR(150),
    adresse VARCHAR(255),
    telephone VARCHAR(30),
    date_naissance date,
    cin VARCHAR(30),
    email VARCHAR(150)
);

CREATE TABLE point_condition (
    id SERIAL,
    libelle VARCHAR(30) NOT NULL,
    CONSTRAINT point_condition_libelle_check CHECK (((libelle)::text = ANY ((ARRAY['SALAIRE', 'DATE_DEBUT', 'HORAIRE', 'LIEU', 'CONDITIONS', 'TYPE_CONTRAT'])::text[])))
);

CREATE TABLE poste (
    id SERIAL,
    code VARCHAR(50) NOT NULL,
    id_metier integer NOT NULL,
    id_echelon integer NOT NULL,
    id_unite_orga integer NOT NULL,
    id_superieur_direct integer,
    libelle VARCHAR(150) NOT NULL,
    etat VARCHAR(20) DEFAULT 'PROPOSE' NOT NULL,
    statut_occupation VARCHAR(20) DEFAULT 'LIBRE' NOT NULL,
    CONSTRAINT poste_etat_check CHECK (((etat)::text = ANY ((ARRAY['PROPOSE', 'ACTIF', 'REFUSE', 'SUPPRIME'])::text[]))),
    CONSTRAINT poste_statut_occupation_check CHECK (((statut_occupation)::text = ANY ((ARRAY['LIBRE', 'OCCUPE'])::text[])))
);

CREATE TABLE proposition_condition (
    id SERIAL,
    id_proposition integer NOT NULL,
    id_point_condition integer NOT NULL,
    valeur text,
    accepte boolean,
    commentaire_candidat text
);

CREATE TABLE proposition_embauche (
    id SERIAL,
    id_candidature integer NOT NULL,
    id_statut_actuel VARCHAR(20) DEFAULT 'PROPOSER' NOT NULL,
    id_besoin_poste integer NOT NULL,
    date_limite_reponse date,
    jeton VARCHAR(64) NOT NULL,
    date_envoi TIMESTAMP,
    date_reponse TIMESTAMP,
    CONSTRAINT proposition_embauche_id_statut_actuel_check CHECK (((id_statut_actuel)::text = ANY ((ARRAY['PROPOSER', 'REFUSER', 'ACCEPTER', 'EXPIREE'])::text[])))
);

CREATE TABLE relance_condition (
    id SERIAL,
    id_proposition_condition integer NOT NULL,
    nouvelle_valeur text,
    statut VARCHAR(30),
    ancienne_valeur text,
    id_user integer,
    date_relance TIMESTAMP DEFAULT now() NOT NULL,
    commentaire text,
    CONSTRAINT ck_relance_condition_statut CHECK (((statut)::text = ANY ((ARRAY['EN_ATTENTE', 'ACCEPTEE', 'REFUSEE'])::text[])))
);

CREATE TABLE role (
    id SERIAL,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE traitement (
    id SERIAL,
    id_candidature integer NOT NULL
);

CREATE TABLE traitement_detail (
    id SERIAL,
    id_traitement integer NOT NULL,
    id_workflow_traitement integer NOT NULL,
    date_prevue date,
    date_realisee date,
    libelle VARCHAR(255),
    id_poste_responsable integer,
    id_user integer,
    score numeric(5,2),
    decision VARCHAR(20),
    commentaire text,
    CONSTRAINT ck_traitement_detail_decision CHECK (((decision IS NULL) OR ((decision)::text = ANY ((ARRAY['ADMIS', 'NON_ADMIS', 'ABSENT'])::text[])))),
    CONSTRAINT ck_traitement_detail_realisee CHECK (((decision IS NULL) OR (date_realisee IS NOT NULL))),
    CONSTRAINT ck_traitement_detail_score CHECK (((score IS NULL) OR ((score >= (0)::numeric) AND (score <= (20)::numeric))))
);

CREATE TABLE type_contrat (
    id SERIAL,
    libelle VARCHAR(50) NOT NULL,
    duree_max integer,
    renouvellement_max integer
);

CREATE TABLE type_document (
    id SERIAL,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE type_information (
    id SERIAL,
    libelle VARCHAR(30) NOT NULL,
    CONSTRAINT type_information_libelle_check CHECK (((libelle)::text = ANY ((ARRAY['EXPERIENCE PRO', 'EDUCATION', 'QUALITE'])::text[])))
);

CREATE TABLE unite_orga (
    id SERIAL,
    code VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL,
    libelle VARCHAR(150) NOT NULL,
    id_unite_mere integer,
    id_poste_responsable integer,
    CONSTRAINT unite_orga_type_check CHECK (((type)::text = ANY ((ARRAY['DIRECTION', 'DEPARTEMENT', 'SERVICE', 'SECTION'])::text[])))
);

CREATE TABLE "user" (
    id SERIAL,
    login VARCHAR(100) NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    id_employe integer,
    statut VARCHAR(20) DEFAULT 'ACTIF' NOT NULL,
    email VARCHAR(150),
    nom_complet VARCHAR(200),
    date_creation TIMESTAMP DEFAULT now() NOT NULL,
    derniere_connexion TIMESTAMP,
    CONSTRAINT ck_user_statut CHECK (((statut)::text = ANY ((ARRAY['ACTIF', 'EN_ATTENTE', 'DESACTIVE'])::text[])))
);

CREATE TABLE user_role (
    id SERIAL,
    id_user integer NOT NULL,
    id_role integer NOT NULL
);

CREATE TABLE validation (
    id SERIAL,
    id_user integer NOT NULL,
    id_besoin integer NOT NULL,
    id_workflow_validation integer NOT NULL,
    decision VARCHAR(30) NOT NULL,
    date TIMESTAMP DEFAULT now() NOT NULL,
    commentaire text
);

CREATE TABLE workflow_candidature (
    id SERIAL,
    ordre integer NOT NULL,
    type_evaluation VARCHAR(100) NOT NULL,
    score_min numeric(5,2),
    id_offre_emploi integer,
    responsable_regle VARCHAR(30),
    id_poste_responsable integer,
    CONSTRAINT ck_workflow_candidature_ordre CHECK ((ordre > 0)),
    CONSTRAINT ck_workflow_candidature_regle CHECK (((responsable_regle IS NULL) OR ((responsable_regle)::text = ANY ((ARRAY['SUPERIEUR', 'RESPONSABLE_UNITE', 'POSTE_FIXE'])::text[])))),
    CONSTRAINT ck_workflow_candidature_score CHECK (((score_min IS NULL) OR ((score_min >= (0)::numeric) AND (score_min <= (20)::numeric)))),
    CONSTRAINT ck_workflow_candidature_type CHECK (((type_evaluation)::text = ANY ((ARRAY['TEST', 'ENTRETIEN_RH', 'ENTRETIEN_CHEF_DEPARTEMENT', 'ENTRETIEN_DIRECTION'])::text[])))
);

CREATE TABLE workflow_onboarding (
    id SERIAL,
    libelle VARCHAR(150) NOT NULL,
    statut VARCHAR(30),
    CONSTRAINT ck_workflow_onboarding_statut CHECK (((statut)::text = ANY ((ARRAY['ACTIF', 'INACTIF'])::text[])))
);

CREATE TABLE workflow_onboarding_details (
    id SERIAL,
    id_workflow integer NOT NULL,
    ordre integer NOT NULL,
    type_tache VARCHAR(100) NOT NULL,
    id_poste_responsable integer,
    delai_jours integer,
    obligatoire boolean DEFAULT true NOT NULL,
    libelle VARCHAR(255)
);

CREATE TABLE workflow_validation (
    id SERIAL,
    id_poste integer NOT NULL,
    ordre integer NOT NULL,
    controle VARCHAR(20),
    CONSTRAINT ck_workflow_validation_controle CHECK (((controle IS NULL) OR ((controle)::text = 'BUDGET'::text)))
);

-- ---------------------------------------------------------------------
-- Clés primaires et contraintes d'unicité
-- ---------------------------------------------------------------------

ALTER TABLE affectation
    ADD CONSTRAINT affectation_pkey PRIMARY KEY (id);

ALTER TABLE besoin
    ADD CONSTRAINT besoin_pkey PRIMARY KEY (id);

ALTER TABLE besoin_poste
    ADD CONSTRAINT besoin_poste_pkey PRIMARY KEY (id);

ALTER TABLE besoin
    ADD CONSTRAINT besoin_reference_key UNIQUE (reference);

ALTER TABLE budget_unite
    ADD CONSTRAINT budget_unite_id_unite_orga_exercice_key UNIQUE (id_unite_orga, exercice);

ALTER TABLE budget_unite
    ADD CONSTRAINT budget_unite_pkey PRIMARY KEY (id);

ALTER TABLE candidat_detail
    ADD CONSTRAINT candidat_detail_pkey PRIMARY KEY (id);

ALTER TABLE candidat
    ADD CONSTRAINT candidat_pkey PRIMARY KEY (id);

ALTER TABLE candidatures
    ADD CONSTRAINT candidatures_id_candidat_id_offre_emploi_key UNIQUE (id_candidat, id_offre_emploi);

ALTER TABLE candidatures
    ADD CONSTRAINT candidatures_pkey PRIMARY KEY (id);

ALTER TABLE categorie_metier
    ADD CONSTRAINT categorie_metier_pkey PRIMARY KEY (id);

ALTER TABLE contrat_details
    ADD CONSTRAINT contrat_details_pkey PRIMARY KEY (id);

ALTER TABLE contrat
    ADD CONSTRAINT contrat_pkey PRIMARY KEY (id);

ALTER TABLE contrat
    ADD CONSTRAINT contrat_reference_key UNIQUE (reference);

ALTER TABLE contrat_remuneration
    ADD CONSTRAINT contrat_remuneration_pkey PRIMARY KEY (id);

ALTER TABLE coordonnees_bancaires
    ADD CONSTRAINT coordonnees_bancaires_pkey PRIMARY KEY (id);

ALTER TABLE delegation
    ADD CONSTRAINT delegation_pkey PRIMARY KEY (id);

ALTER TABLE demande_contrat
    ADD CONSTRAINT demande_contrat_pkey PRIMARY KEY (id);

ALTER TABLE demande_contrat
    ADD CONSTRAINT demande_contrat_reference_key UNIQUE (reference);

ALTER TABLE document_candidat
    ADD CONSTRAINT document_candidat_pkey PRIMARY KEY (id);

ALTER TABLE document
    ADD CONSTRAINT document_pkey PRIMARY KEY (id);

ALTER TABLE echelon_metier
    ADD CONSTRAINT echelon_metier_pkey PRIMARY KEY (id);

ALTER TABLE employe
    ADD CONSTRAINT employe_matricule_key UNIQUE (matricule);

ALTER TABLE employe
    ADD CONSTRAINT employe_pkey PRIMARY KEY (id);

ALTER TABLE etat_contrat
    ADD CONSTRAINT etat_contrat_pkey PRIMARY KEY (id);

ALTER TABLE fiche_poste
    ADD CONSTRAINT fiche_poste_pkey PRIMARY KEY (id);

ALTER TABLE grille_salariale_details
    ADD CONSTRAINT grille_salariale_details_id_grille_id_categorie_id_echelon_key UNIQUE (id_grille, id_categorie, id_echelon);

ALTER TABLE grille_salariale_details
    ADD CONSTRAINT grille_salariale_details_pkey PRIMARY KEY (id);

ALTER TABLE grille_salariale
    ADD CONSTRAINT grille_salariale_pkey PRIMARY KEY (id);

ALTER TABLE histo_besoin
    ADD CONSTRAINT histo_besoin_pkey PRIMARY KEY (id);

ALTER TABLE histo_candidature
    ADD CONSTRAINT histo_candidature_pkey PRIMARY KEY (id);

ALTER TABLE histo_proposition_embauche
    ADD CONSTRAINT histo_proposition_embauche_pkey PRIMARY KEY (id);

ALTER TABLE journal_audit
    ADD CONSTRAINT journal_audit_pkey PRIMARY KEY (id);

ALTER TABLE metier
    ADD CONSTRAINT metier_pkey PRIMARY KEY (id);

ALTER TABLE motif
    ADD CONSTRAINT motif_pkey PRIMARY KEY (id);

ALTER TABLE notification
    ADD CONSTRAINT notification_pkey PRIMARY KEY (id);

ALTER TABLE offre_emploi
    ADD CONSTRAINT offre_emploi_pkey PRIMARY KEY (id);

ALTER TABLE offre_emploi
    ADD CONSTRAINT offre_emploi_ref_key UNIQUE (ref);

ALTER TABLE onboarding_details
    ADD CONSTRAINT onboarding_details_pkey PRIMARY KEY (id);

ALTER TABLE onboarding
    ADD CONSTRAINT onboarding_pkey PRIMARY KEY (id);

ALTER TABLE personne
    ADD CONSTRAINT personne_cin_key UNIQUE (cin);

ALTER TABLE personne
    ADD CONSTRAINT personne_pkey PRIMARY KEY (id);

ALTER TABLE point_condition
    ADD CONSTRAINT point_condition_libelle_key UNIQUE (libelle);

ALTER TABLE point_condition
    ADD CONSTRAINT point_condition_pkey PRIMARY KEY (id);

ALTER TABLE poste
    ADD CONSTRAINT poste_code_key UNIQUE (code);

ALTER TABLE poste
    ADD CONSTRAINT poste_pkey PRIMARY KEY (id);

ALTER TABLE proposition_condition
    ADD CONSTRAINT proposition_condition_id_proposition_id_point_condition_key UNIQUE (id_proposition, id_point_condition);

ALTER TABLE proposition_condition
    ADD CONSTRAINT proposition_condition_pkey PRIMARY KEY (id);

ALTER TABLE proposition_embauche
    ADD CONSTRAINT proposition_embauche_pkey PRIMARY KEY (id);

ALTER TABLE relance_condition
    ADD CONSTRAINT relance_condition_pkey PRIMARY KEY (id);

ALTER TABLE role
    ADD CONSTRAINT role_libelle_key UNIQUE (libelle);

ALTER TABLE role
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);

ALTER TABLE traitement_detail
    ADD CONSTRAINT traitement_detail_pkey PRIMARY KEY (id);

ALTER TABLE traitement
    ADD CONSTRAINT traitement_pkey PRIMARY KEY (id);

ALTER TABLE type_contrat
    ADD CONSTRAINT type_contrat_libelle_key UNIQUE (libelle);

ALTER TABLE type_contrat
    ADD CONSTRAINT type_contrat_pkey PRIMARY KEY (id);

ALTER TABLE type_document
    ADD CONSTRAINT type_document_pkey PRIMARY KEY (id);

ALTER TABLE type_information
    ADD CONSTRAINT type_information_libelle_key UNIQUE (libelle);

ALTER TABLE type_information
    ADD CONSTRAINT type_information_pkey PRIMARY KEY (id);

ALTER TABLE unite_orga
    ADD CONSTRAINT unite_orga_code_key UNIQUE (code);

ALTER TABLE unite_orga
    ADD CONSTRAINT unite_orga_pkey PRIMARY KEY (id);

ALTER TABLE "user"
    ADD CONSTRAINT user_login_key UNIQUE (login);

ALTER TABLE "user"
    ADD CONSTRAINT user_pkey PRIMARY KEY (id);

ALTER TABLE user_role
    ADD CONSTRAINT user_role_id_user_id_role_key UNIQUE (id_user, id_role);

ALTER TABLE user_role
    ADD CONSTRAINT user_role_pkey PRIMARY KEY (id);

ALTER TABLE validation
    ADD CONSTRAINT validation_pkey PRIMARY KEY (id);

ALTER TABLE workflow_candidature
    ADD CONSTRAINT workflow_candidature_pkey PRIMARY KEY (id);

ALTER TABLE workflow_onboarding_details
    ADD CONSTRAINT workflow_onboarding_details_id_workflow_ordre_key UNIQUE (id_workflow, ordre);

ALTER TABLE workflow_onboarding_details
    ADD CONSTRAINT workflow_onboarding_details_pkey PRIMARY KEY (id);

ALTER TABLE workflow_onboarding
    ADD CONSTRAINT workflow_onboarding_pkey PRIMARY KEY (id);

ALTER TABLE workflow_validation
    ADD CONSTRAINT workflow_validation_id_poste_ordre_key UNIQUE (id_poste, ordre);

ALTER TABLE workflow_validation
    ADD CONSTRAINT workflow_validation_pkey PRIMARY KEY (id);

-- ---------------------------------------------------------------------
-- Index
-- ---------------------------------------------------------------------

CREATE INDEX idx_affectation_poste ON affectation USING btree (id_poste);

CREATE INDEX idx_besoin_unite ON besoin USING btree (id_unite_orga);

CREATE INDEX idx_candidature_offre ON candidatures USING btree (id_offre_emploi);

CREATE INDEX idx_contrat_affect ON contrat USING btree (id_affectation);

CREATE INDEX idx_coordonnees_bancaires_employe ON coordonnees_bancaires USING btree (id_employe);

CREATE INDEX idx_demande_contrat_contrat ON demande_contrat USING btree (id_contrat);

CREATE INDEX idx_demande_contrat_etat ON demande_contrat USING btree (etat);

CREATE INDEX idx_histo_besoin_besoin ON histo_besoin USING btree (id_besoin, date_action);

CREATE INDEX idx_histo_candidature ON histo_candidature USING btree (id_candidature, date_changement);

CREATE INDEX idx_histo_proposition ON histo_proposition_embauche USING btree (id_proposition_embauche, date);

CREATE INDEX idx_journal_audit_date ON journal_audit USING btree (date_action DESC);

CREATE INDEX idx_notification_type_lien ON notification USING btree (type, lien);

CREATE INDEX idx_notification_user_date ON notification USING btree (id_utilisateur, date_creation DESC);

CREATE INDEX idx_notification_user_lu ON notification USING btree (id_utilisateur, lu);

CREATE INDEX idx_offre_emploi_besoin_poste ON offre_emploi USING btree (id_besoin, id_poste);

CREATE INDEX idx_poste_unite ON poste USING btree (id_unite_orga);

CREATE INDEX idx_relance_condition ON relance_condition USING btree (id_proposition_condition);

CREATE INDEX idx_unite_mere ON unite_orga USING btree (id_unite_mere);

CREATE UNIQUE INDEX uq_document_candidat ON document_candidat USING btree (id_candidature, id_document);

CREATE UNIQUE INDEX ux_candidat_email ON candidat USING btree (lower((email)::text));

CREATE UNIQUE INDEX ux_fiche_poste_metier ON fiche_poste USING btree (id_metier) WHERE (id_metier IS NOT NULL);

CREATE UNIQUE INDEX ux_fiche_poste_poste ON fiche_poste USING btree (id_poste) WHERE (id_poste IS NOT NULL);

CREATE UNIQUE INDEX ux_onboarding_contrat ON onboarding USING btree (id_contrat);

CREATE UNIQUE INDEX ux_proposition_candidature_en_cours ON proposition_embauche USING btree (id_candidature) WHERE ((id_statut_actuel)::text = ANY ((ARRAY['PROPOSER', 'ACCEPTER'])::text[]));

CREATE UNIQUE INDEX ux_proposition_embauche_jeton ON proposition_embauche USING btree (jeton);

CREATE UNIQUE INDEX ux_proposition_siege_en_cours ON proposition_embauche USING btree (id_besoin_poste) WHERE ((id_statut_actuel)::text = ANY ((ARRAY['PROPOSER', 'ACCEPTER'])::text[]));

CREATE UNIQUE INDEX ux_traitement_candidature ON traitement USING btree (id_candidature);

CREATE UNIQUE INDEX ux_traitement_detail_niveau ON traitement_detail USING btree (id_traitement, id_workflow_traitement);

CREATE UNIQUE INDEX ux_workflow_candidature_ordre ON workflow_candidature USING btree (COALESCE(id_offre_emploi, 0), ordre);

-- ---------------------------------------------------------------------
-- Clés étrangères (DEFERRABLE : permet d'insérer les données des tables qui se référencent mutuellement, ex. unite_orga <-> poste)
-- ---------------------------------------------------------------------

ALTER TABLE affectation
    ADD CONSTRAINT affectation_id_employe_fkey FOREIGN KEY (id_employe) REFERENCES employe(id) DEFERRABLE;

ALTER TABLE affectation
    ADD CONSTRAINT affectation_id_poste_fkey FOREIGN KEY (id_poste) REFERENCES poste(id) DEFERRABLE;

ALTER TABLE besoin
    ADD CONSTRAINT besoin_id_motif_fkey FOREIGN KEY (id_motif) REFERENCES motif(id) DEFERRABLE;

ALTER TABLE besoin
    ADD CONSTRAINT besoin_id_unite_orga_fkey FOREIGN KEY (id_unite_orga) REFERENCES unite_orga(id) DEFERRABLE;

ALTER TABLE besoin
    ADD CONSTRAINT besoin_id_user_fkey FOREIGN KEY (id_user) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE besoin_poste
    ADD CONSTRAINT besoin_poste_id_besoin_fkey FOREIGN KEY (id_besoin) REFERENCES besoin(id) DEFERRABLE;

ALTER TABLE besoin_poste
    ADD CONSTRAINT besoin_poste_id_poste_fkey FOREIGN KEY (id_poste) REFERENCES poste(id) DEFERRABLE;

ALTER TABLE budget_unite
    ADD CONSTRAINT budget_unite_id_unite_orga_fkey FOREIGN KEY (id_unite_orga) REFERENCES unite_orga(id) DEFERRABLE;

ALTER TABLE budget_unite
    ADD CONSTRAINT budget_unite_id_user_decision_fkey FOREIGN KEY (id_user_decision) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE budget_unite
    ADD CONSTRAINT budget_unite_id_user_propose_fkey FOREIGN KEY (id_user_propose) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE candidat_detail
    ADD CONSTRAINT candidat_detail_id_candidat_fkey FOREIGN KEY (id_candidat) REFERENCES candidat(id) DEFERRABLE;

ALTER TABLE candidat_detail
    ADD CONSTRAINT candidat_detail_id_type_information_fkey FOREIGN KEY (id_type_information) REFERENCES type_information(id) DEFERRABLE;

ALTER TABLE candidat
    ADD CONSTRAINT candidat_id_personne_fkey FOREIGN KEY (id_personne) REFERENCES personne(id) DEFERRABLE;

ALTER TABLE candidatures
    ADD CONSTRAINT candidatures_id_candidat_fkey FOREIGN KEY (id_candidat) REFERENCES candidat(id) DEFERRABLE;

ALTER TABLE candidatures
    ADD CONSTRAINT candidatures_id_offre_emploi_fkey FOREIGN KEY (id_offre_emploi) REFERENCES offre_emploi(id) DEFERRABLE;

ALTER TABLE contrat_details
    ADD CONSTRAINT contrat_details_id_contrat_fkey FOREIGN KEY (id_contrat) REFERENCES contrat(id) DEFERRABLE;

ALTER TABLE contrat_details
    ADD CONSTRAINT contrat_details_id_point_condition_fkey FOREIGN KEY (id_point_condition) REFERENCES point_condition(id) DEFERRABLE;

ALTER TABLE contrat
    ADD CONSTRAINT contrat_id_affectation_fkey FOREIGN KEY (id_affectation) REFERENCES affectation(id) DEFERRABLE;

ALTER TABLE contrat
    ADD CONSTRAINT contrat_id_contrat_precedent_fkey FOREIGN KEY (id_contrat_precedent) REFERENCES contrat(id) DEFERRABLE;

ALTER TABLE contrat
    ADD CONSTRAINT contrat_id_document_fkey FOREIGN KEY (id_document) REFERENCES document(id) DEFERRABLE;

ALTER TABLE contrat
    ADD CONSTRAINT contrat_id_proposition_fkey FOREIGN KEY (id_proposition) REFERENCES proposition_embauche(id) DEFERRABLE;

ALTER TABLE contrat
    ADD CONSTRAINT contrat_id_type_fkey FOREIGN KEY (id_type) REFERENCES type_contrat(id) DEFERRABLE;

ALTER TABLE contrat_remuneration
    ADD CONSTRAINT contrat_remuneration_id_contrat_fkey FOREIGN KEY (id_contrat) REFERENCES contrat(id) DEFERRABLE;

ALTER TABLE contrat_remuneration
    ADD CONSTRAINT contrat_remuneration_id_echelon_fkey FOREIGN KEY (id_echelon) REFERENCES echelon_metier(id) DEFERRABLE;

ALTER TABLE coordonnees_bancaires
    ADD CONSTRAINT coordonnees_bancaires_id_employe_fkey FOREIGN KEY (id_employe) REFERENCES employe(id) DEFERRABLE;

ALTER TABLE delegation
    ADD CONSTRAINT delegation_id_user_delegant_fkey FOREIGN KEY (id_user_delegant) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE delegation
    ADD CONSTRAINT delegation_id_user_delegue_fkey FOREIGN KEY (id_user_delegue) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE demande_contrat
    ADD CONSTRAINT demande_contrat_id_contrat_fkey FOREIGN KEY (id_contrat) REFERENCES contrat(id) DEFERRABLE;

ALTER TABLE demande_contrat
    ADD CONSTRAINT demande_contrat_id_contrat_resultat_fkey FOREIGN KEY (id_contrat_resultat) REFERENCES contrat(id) DEFERRABLE;

ALTER TABLE demande_contrat
    ADD CONSTRAINT demande_contrat_id_demandeur_fkey FOREIGN KEY (id_demandeur) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE demande_contrat
    ADD CONSTRAINT demande_contrat_id_dg_fkey FOREIGN KEY (id_dg) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE demande_contrat
    ADD CONSTRAINT demande_contrat_id_finance_fkey FOREIGN KEY (id_finance) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE demande_contrat
    ADD CONSTRAINT demande_contrat_id_rh_fkey FOREIGN KEY (id_rh) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE demande_contrat
    ADD CONSTRAINT demande_contrat_id_type_contrat_fkey FOREIGN KEY (id_type_contrat) REFERENCES type_contrat(id) DEFERRABLE;

ALTER TABLE document_candidat
    ADD CONSTRAINT document_candidat_id_candidature_fkey FOREIGN KEY (id_candidature) REFERENCES candidatures(id) DEFERRABLE;

ALTER TABLE document_candidat
    ADD CONSTRAINT document_candidat_id_document_fkey FOREIGN KEY (id_document) REFERENCES document(id) DEFERRABLE;

ALTER TABLE document
    ADD CONSTRAINT document_id_type_document_fkey FOREIGN KEY (id_type_document) REFERENCES type_document(id) DEFERRABLE;

ALTER TABLE employe
    ADD CONSTRAINT employe_id_personne_fkey FOREIGN KEY (id_personne) REFERENCES personne(id) DEFERRABLE;

ALTER TABLE etat_contrat
    ADD CONSTRAINT etat_contrat_id_contrat_fkey FOREIGN KEY (id_contrat) REFERENCES contrat(id) DEFERRABLE;

ALTER TABLE fiche_poste
    ADD CONSTRAINT fiche_poste_id_auteur_fkey FOREIGN KEY (id_auteur) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE fiche_poste
    ADD CONSTRAINT fiche_poste_id_metier_fkey FOREIGN KEY (id_metier) REFERENCES metier(id) DEFERRABLE;

ALTER TABLE fiche_poste
    ADD CONSTRAINT fiche_poste_id_poste_fkey FOREIGN KEY (id_poste) REFERENCES poste(id) DEFERRABLE;

ALTER TABLE besoin_poste
    ADD CONSTRAINT fk_besoin_poste_candidature FOREIGN KEY (id_candidature_retenue) REFERENCES candidatures(id) DEFERRABLE;

ALTER TABLE besoin_poste
    ADD CONSTRAINT fk_besoin_poste_employe_remplace FOREIGN KEY (id_employe_remplace) REFERENCES employe(id) DEFERRABLE;

ALTER TABLE unite_orga
    ADD CONSTRAINT fk_unite_orga_poste_resp FOREIGN KEY (id_poste_responsable) REFERENCES poste(id) DEFERRABLE;

ALTER TABLE grille_salariale_details
    ADD CONSTRAINT grille_salariale_details_id_categorie_fkey FOREIGN KEY (id_categorie) REFERENCES categorie_metier(id) DEFERRABLE;

ALTER TABLE grille_salariale_details
    ADD CONSTRAINT grille_salariale_details_id_echelon_fkey FOREIGN KEY (id_echelon) REFERENCES echelon_metier(id) DEFERRABLE;

ALTER TABLE grille_salariale_details
    ADD CONSTRAINT grille_salariale_details_id_grille_fkey FOREIGN KEY (id_grille) REFERENCES grille_salariale(id) DEFERRABLE;

ALTER TABLE grille_salariale
    ADD CONSTRAINT grille_salariale_id_user_decision_fkey FOREIGN KEY (id_user_decision) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE grille_salariale
    ADD CONSTRAINT grille_salariale_id_user_propose_fkey FOREIGN KEY (id_user_propose) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE histo_besoin
    ADD CONSTRAINT histo_besoin_id_besoin_fkey FOREIGN KEY (id_besoin) REFERENCES besoin(id) DEFERRABLE;

ALTER TABLE histo_besoin
    ADD CONSTRAINT histo_besoin_id_user_fkey FOREIGN KEY (id_user) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE histo_candidature
    ADD CONSTRAINT histo_candidature_id_candidature_fkey FOREIGN KEY (id_candidature) REFERENCES candidatures(id) DEFERRABLE;

ALTER TABLE histo_candidature
    ADD CONSTRAINT histo_candidature_id_user_fkey FOREIGN KEY (id_user) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE histo_proposition_embauche
    ADD CONSTRAINT histo_proposition_embauche_id_proposition_embauche_fkey FOREIGN KEY (id_proposition_embauche) REFERENCES proposition_embauche(id) DEFERRABLE;

ALTER TABLE histo_proposition_embauche
    ADD CONSTRAINT histo_proposition_embauche_id_user_fkey FOREIGN KEY (id_user) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE journal_audit
    ADD CONSTRAINT journal_audit_id_user_fkey FOREIGN KEY (id_user) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE metier
    ADD CONSTRAINT metier_id_categorie_fkey FOREIGN KEY (id_categorie) REFERENCES categorie_metier(id) DEFERRABLE;

ALTER TABLE notification
    ADD CONSTRAINT notification_id_utilisateur_fkey FOREIGN KEY (id_utilisateur) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE offre_emploi
    ADD CONSTRAINT offre_emploi_id_besoin_fkey FOREIGN KEY (id_besoin) REFERENCES besoin(id) DEFERRABLE;

ALTER TABLE offre_emploi
    ADD CONSTRAINT offre_emploi_id_document_fkey FOREIGN KEY (id_document) REFERENCES document(id) DEFERRABLE;

ALTER TABLE offre_emploi
    ADD CONSTRAINT offre_emploi_id_fiche_poste_fkey FOREIGN KEY (id_fiche_poste) REFERENCES fiche_poste(id) DEFERRABLE;

ALTER TABLE offre_emploi
    ADD CONSTRAINT offre_emploi_id_poste_fkey FOREIGN KEY (id_poste) REFERENCES poste(id) DEFERRABLE;

ALTER TABLE offre_emploi
    ADD CONSTRAINT offre_emploi_id_type_contrat_fkey FOREIGN KEY (id_type_contrat) REFERENCES type_contrat(id) DEFERRABLE;

ALTER TABLE offre_emploi
    ADD CONSTRAINT offre_emploi_id_user_cloture_fkey FOREIGN KEY (id_user_cloture) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE onboarding_details
    ADD CONSTRAINT onboarding_details_id_onboarding_fkey FOREIGN KEY (id_onboarding) REFERENCES onboarding(id) DEFERRABLE;

ALTER TABLE onboarding_details
    ADD CONSTRAINT onboarding_details_id_poste_responsable_fkey FOREIGN KEY (id_poste_responsable) REFERENCES poste(id) DEFERRABLE;

ALTER TABLE onboarding_details
    ADD CONSTRAINT onboarding_details_id_user_realisateur_fkey FOREIGN KEY (id_user_realisateur) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE onboarding_details
    ADD CONSTRAINT onboarding_details_id_workflow_details_fkey FOREIGN KEY (id_workflow_details) REFERENCES workflow_onboarding_details(id) DEFERRABLE;

ALTER TABLE onboarding
    ADD CONSTRAINT onboarding_id_contrat_fkey FOREIGN KEY (id_contrat) REFERENCES contrat(id) DEFERRABLE;

ALTER TABLE onboarding
    ADD CONSTRAINT onboarding_id_workflow_fkey FOREIGN KEY (id_workflow) REFERENCES workflow_onboarding(id) DEFERRABLE;

ALTER TABLE poste
    ADD CONSTRAINT poste_id_echelon_fkey FOREIGN KEY (id_echelon) REFERENCES echelon_metier(id) DEFERRABLE;

ALTER TABLE poste
    ADD CONSTRAINT poste_id_metier_fkey FOREIGN KEY (id_metier) REFERENCES metier(id) DEFERRABLE;

ALTER TABLE poste
    ADD CONSTRAINT poste_id_superieur_direct_fkey FOREIGN KEY (id_superieur_direct) REFERENCES poste(id) DEFERRABLE;

ALTER TABLE poste
    ADD CONSTRAINT poste_id_unite_orga_fkey FOREIGN KEY (id_unite_orga) REFERENCES unite_orga(id) DEFERRABLE;

ALTER TABLE proposition_condition
    ADD CONSTRAINT proposition_condition_id_point_condition_fkey FOREIGN KEY (id_point_condition) REFERENCES point_condition(id) DEFERRABLE;

ALTER TABLE proposition_condition
    ADD CONSTRAINT proposition_condition_id_proposition_fkey FOREIGN KEY (id_proposition) REFERENCES proposition_embauche(id) DEFERRABLE;

ALTER TABLE proposition_embauche
    ADD CONSTRAINT proposition_embauche_id_besoin_poste_fkey FOREIGN KEY (id_besoin_poste) REFERENCES besoin_poste(id) DEFERRABLE;

ALTER TABLE proposition_embauche
    ADD CONSTRAINT proposition_embauche_id_candidature_fkey FOREIGN KEY (id_candidature) REFERENCES candidatures(id) DEFERRABLE;

ALTER TABLE relance_condition
    ADD CONSTRAINT relance_condition_id_proposition_condition_fkey FOREIGN KEY (id_proposition_condition) REFERENCES proposition_condition(id) DEFERRABLE;

ALTER TABLE relance_condition
    ADD CONSTRAINT relance_condition_id_user_fkey FOREIGN KEY (id_user) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE traitement_detail
    ADD CONSTRAINT traitement_detail_id_poste_responsable_fkey FOREIGN KEY (id_poste_responsable) REFERENCES poste(id) DEFERRABLE;

ALTER TABLE traitement_detail
    ADD CONSTRAINT traitement_detail_id_traitement_fkey FOREIGN KEY (id_traitement) REFERENCES traitement(id) DEFERRABLE;

ALTER TABLE traitement_detail
    ADD CONSTRAINT traitement_detail_id_user_fkey FOREIGN KEY (id_user) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE traitement_detail
    ADD CONSTRAINT traitement_detail_id_workflow_traitement_fkey FOREIGN KEY (id_workflow_traitement) REFERENCES workflow_candidature(id) DEFERRABLE;

ALTER TABLE traitement
    ADD CONSTRAINT traitement_id_candidature_fkey FOREIGN KEY (id_candidature) REFERENCES candidatures(id) DEFERRABLE;

ALTER TABLE unite_orga
    ADD CONSTRAINT unite_orga_id_unite_mere_fkey FOREIGN KEY (id_unite_mere) REFERENCES unite_orga(id) DEFERRABLE;

ALTER TABLE "user"
    ADD CONSTRAINT user_id_employe_fkey FOREIGN KEY (id_employe) REFERENCES employe(id) DEFERRABLE;

ALTER TABLE user_role
    ADD CONSTRAINT user_role_id_role_fkey FOREIGN KEY (id_role) REFERENCES role(id) DEFERRABLE;

ALTER TABLE user_role
    ADD CONSTRAINT user_role_id_user_fkey FOREIGN KEY (id_user) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE validation
    ADD CONSTRAINT validation_id_besoin_fkey FOREIGN KEY (id_besoin) REFERENCES besoin(id) DEFERRABLE;

ALTER TABLE validation
    ADD CONSTRAINT validation_id_user_fkey FOREIGN KEY (id_user) REFERENCES "user"(id) DEFERRABLE;

ALTER TABLE validation
    ADD CONSTRAINT validation_id_workflow_validation_fkey FOREIGN KEY (id_workflow_validation) REFERENCES workflow_validation(id) DEFERRABLE;

ALTER TABLE workflow_candidature
    ADD CONSTRAINT workflow_candidature_id_offre_emploi_fkey FOREIGN KEY (id_offre_emploi) REFERENCES offre_emploi(id) DEFERRABLE;

ALTER TABLE workflow_candidature
    ADD CONSTRAINT workflow_candidature_id_poste_responsable_fkey FOREIGN KEY (id_poste_responsable) REFERENCES poste(id) DEFERRABLE;

ALTER TABLE workflow_onboarding_details
    ADD CONSTRAINT workflow_onboarding_details_id_poste_responsable_fkey FOREIGN KEY (id_poste_responsable) REFERENCES poste(id) DEFERRABLE;

ALTER TABLE workflow_onboarding_details
    ADD CONSTRAINT workflow_onboarding_details_id_workflow_fkey FOREIGN KEY (id_workflow) REFERENCES workflow_onboarding(id) DEFERRABLE;

ALTER TABLE workflow_validation
    ADD CONSTRAINT workflow_validation_id_poste_fkey FOREIGN KEY (id_poste) REFERENCES poste(id) DEFERRABLE;
