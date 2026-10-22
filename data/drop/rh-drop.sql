-- =====================================================================
-- DROP du schéma RH (ordre inverse des dépendances)
-- =====================================================================

BEGIN;

-- --- BLOC 4 : CONTRAT & ONBOARDING ---
DROP TABLE IF EXISTS onboarding_details            CASCADE;
DROP TABLE IF EXISTS onboarding                    CASCADE;
DROP TABLE IF EXISTS workflow_onboarding_details   CASCADE;
DROP TABLE IF EXISTS workflow_onboarding           CASCADE;
DROP TABLE IF EXISTS etat_contrat                  CASCADE;
DROP TABLE IF EXISTS contrat_remuneration          CASCADE;
DROP TABLE IF EXISTS contrat_details               CASCADE;
DROP TABLE IF EXISTS contrat                       CASCADE;

-- --- BLOC 3 : SÉLECTION & EMBAUCHE ---
DROP TABLE IF EXISTS histo_proposition_embauche    CASCADE;
DROP TABLE IF EXISTS relance_condition             CASCADE;
DROP TABLE IF EXISTS proposition_condition         CASCADE;
DROP TABLE IF EXISTS proposition_embauche          CASCADE;
DROP TABLE IF EXISTS point_condition               CASCADE;

-- --- BLOC 2 : RECRUTEMENT ---
DROP TABLE IF EXISTS histo_candidature             CASCADE;   -- P5 sprint 2
DROP TABLE IF EXISTS traitement_detail             CASCADE;
DROP TABLE IF EXISTS traitement                    CASCADE;
DROP TABLE IF EXISTS workflow_candidature          CASCADE;
DROP TABLE IF EXISTS candidat_detail               CASCADE;
DROP TABLE IF EXISTS type_information              CASCADE;
DROP TABLE IF EXISTS document_candidat             CASCADE;
DROP TABLE IF EXISTS candidatures                  CASCADE;
DROP TABLE IF EXISTS candidat                      CASCADE;
DROP TABLE IF EXISTS offre_emploi                  CASCADE;
DROP TABLE IF EXISTS type_contrat                  CASCADE;
DROP TABLE IF EXISTS document                      CASCADE;
DROP TABLE IF EXISTS type_document                 CASCADE;

-- --- BLOC 1 : BESOIN ---
DROP TABLE IF EXISTS validation                    CASCADE;
DROP TABLE IF EXISTS workflow_validation           CASCADE;
DROP TABLE IF EXISTS besoin_poste                  CASCADE;
DROP TABLE IF EXISTS besoin                        CASCADE;
DROP TABLE IF EXISTS motif                         CASCADE;

-- --- UTILISATEURS & RÔLES ---
DROP TABLE IF EXISTS user_role                     CASCADE;
DROP TABLE IF EXISTS role                          CASCADE;
DROP TABLE IF EXISTS "user"                        CASCADE;

-- --- GRILLE SALARIALE & BUDGET ---
DROP TABLE IF EXISTS budget_unite                  CASCADE;
DROP TABLE IF EXISTS grille_salariale_details      CASCADE;
DROP TABLE IF EXISTS grille_salariale              CASCADE;

-- --- STRUCTURE ORGANISATIONNELLE ---
DROP TABLE IF EXISTS affectation                   CASCADE;
DROP TABLE IF EXISTS employe                       CASCADE;
DROP TABLE IF EXISTS poste                         CASCADE;
DROP TABLE IF EXISTS metier                        CASCADE;
DROP TABLE IF EXISTS echelon_metier                CASCADE;
DROP TABLE IF EXISTS categorie_metier              CASCADE;
DROP TABLE IF EXISTS unite_orga                    CASCADE;

COMMIT;