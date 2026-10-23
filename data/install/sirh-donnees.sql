-- =====================================================================
-- SIRH – données de référence et données de test
-- À exécuter après sirh-schema.sql, sur une base vide.
-- Comptes de test (mots de passe en clair ici, remplacés par leur empreinte PBKDF2 à la première connexion) :
--   admin / hash_demo_admin               manager.dev / hash_demo_manager_dev
--   rh.drh / hash_demo_rh                 finance.daf / hash_demo_finance
--   dg / hash_demo_dg                     manager.compta / hash_demo_manager_compta
--   paie / hash_demo_paie                 support / hash_demo_support
-- =====================================================================

SET search_path = public;
BEGIN;
-- unite_orga et poste se référencent mutuellement : contrôle des clés étrangères à la fin de la transaction
SET CONSTRAINTS ALL DEFERRED;

-- categorie_metier (3)
INSERT INTO categorie_metier (id, libelle) VALUES (1, 'Cadre');
INSERT INTO categorie_metier (id, libelle) VALUES (2, 'Agent de maitrise');
INSERT INTO categorie_metier (id, libelle) VALUES (3, 'Employe');

-- echelon_metier (3)
INSERT INTO echelon_metier (id, libelle) VALUES (1, 'Junior');
INSERT INTO echelon_metier (id, libelle) VALUES (2, 'Confirme');
INSERT INTO echelon_metier (id, libelle) VALUES (3, 'Senior');

-- personne (3)
INSERT INTO personne (id, nom, prenoms, adresse, telephone, date_naissance, cin, email) VALUES (1, 'Rakoto', 'Paul', NULL, '0343333333', '1993-11-20', NULL, 'rakoto.test@mail.mg');
INSERT INTO personne (id, nom, prenoms, adresse, telephone, date_naissance, cin, email) VALUES (2, 'Rasoa', 'Marie', NULL, '0342222222', '1998-07-15', NULL, 'rasoa.test@mail.mg');
INSERT INTO personne (id, nom, prenoms, adresse, telephone, date_naissance, cin, email) VALUES (3, 'Rabe', 'Jean', NULL, '0341111111', '1995-03-02', NULL, 'rabe.test@mail.mg');

-- employe (18)
INSERT INTO employe (id, matricule, nom, prenoms, adresse, id_personne) VALUES (1, 'EMP-2026-001', 'RAKOTO', 'Jean Michel', 'Antananarivo', NULL);
INSERT INTO employe (id, matricule, nom, prenoms, adresse, id_personne) VALUES (2, 'EMP-2026-002', 'RANDRIAMAMPIANINA', 'Sarah', 'Antananarivo', NULL);
INSERT INTO employe (id, matricule, nom, prenoms, adresse, id_personne) VALUES (3, 'EMP-2026-003', 'RAZAFINDRAKOTO', 'Hery', 'Ambohimangakely', NULL);
INSERT INTO employe (id, matricule, nom, prenoms, adresse, id_personne) VALUES (4, 'EMP-2026-004', 'RABEMANANJARA', 'Miora', 'Ivandry', NULL);
INSERT INTO employe (id, matricule, nom, prenoms, adresse, id_personne) VALUES (5, 'EMP-2026-005', 'RAKOTONDRABE', 'Tojo', 'Ankorondrano', NULL);
INSERT INTO employe (id, matricule, nom, prenoms, adresse, id_personne) VALUES (6, 'EMP-2026-006', 'ANDRIANASOLO', 'Feno', 'Soanierana', NULL);
INSERT INTO employe (id, matricule, nom, prenoms, adresse, id_personne) VALUES (7, 'EMP-2026-007', 'RASOLOFOARISOA', 'Nantenaina', 'Tanjombato', NULL);
INSERT INTO employe (id, matricule, nom, prenoms, adresse, id_personne) VALUES (8, 'EMP-2026-008', 'RAZAFIMALALA', 'Mamy', 'Andraharo', NULL);
INSERT INTO employe (id, matricule, nom, prenoms, adresse, id_personne) VALUES (9, 'EMP-2026-009', 'RANDRIANARISOA', 'Lova', 'Ambohidratrimo', NULL);
INSERT INTO employe (id, matricule, nom, prenoms, adresse, id_personne) VALUES (10, 'EMP-2026-010', 'RAKOTOMALALA', 'Tiana', 'Alarobia', NULL);
INSERT INTO employe (id, matricule, nom, prenoms, adresse, id_personne) VALUES (11, 'EMP-2026-011', 'RAZAFINDRAIBE', 'Tsiry', 'Ankadifotsy', NULL);
INSERT INTO employe (id, matricule, nom, prenoms, adresse, id_personne) VALUES (12, 'EMP-2026-012', 'RABARY', 'Nirina', 'Mahamasina', NULL);
INSERT INTO employe (id, matricule, nom, prenoms, adresse, id_personne) VALUES (13, 'EMP-2026-013', 'ANDRIAMBOLOLONA', 'Fara', 'Tsaralalana', NULL);
INSERT INTO employe (id, matricule, nom, prenoms, adresse, id_personne) VALUES (14, 'EMP-2026-014', 'RAKOTOARISOA', 'Haja', 'Behoririka', NULL);
INSERT INTO employe (id, matricule, nom, prenoms, adresse, id_personne) VALUES (15, 'EMP-2026-015', 'RAZANADRAKOTO', 'Aina', 'Ambodimita', NULL);
INSERT INTO employe (id, matricule, nom, prenoms, adresse, id_personne) VALUES (16, 'EMP-2026-016', 'RANDRIANANTENAINA', 'Voahirana', 'Anosizato', NULL);
INSERT INTO employe (id, matricule, nom, prenoms, adresse, id_personne) VALUES (17, 'EMP-2026-017', 'RAKOTONIAINA', 'Lanto', 'Fianarantsoa', NULL);
INSERT INTO employe (id, matricule, nom, prenoms, adresse, id_personne) VALUES (18, 'EMP-2026-018', 'ANDRIANJAFY', 'Hasina', 'Antsirabe', NULL);

-- metier (10)
INSERT INTO metier (id, libelle, id_categorie) VALUES (1, 'Directeur general', 1);
INSERT INTO metier (id, libelle, id_categorie) VALUES (2, 'Directeur', 1);
INSERT INTO metier (id, libelle, id_categorie) VALUES (3, 'Chef de departement', 1);
INSERT INTO metier (id, libelle, id_categorie) VALUES (4, 'Chef de service', 2);
INSERT INTO metier (id, libelle, id_categorie) VALUES (5, 'Comptable', 2);
INSERT INTO metier (id, libelle, id_categorie) VALUES (6, 'Developpeur', 2);
INSERT INTO metier (id, libelle, id_categorie) VALUES (7, 'Administrateur reseau', 2);
INSERT INTO metier (id, libelle, id_categorie) VALUES (8, 'Technicien support', 3);
INSERT INTO metier (id, libelle, id_categorie) VALUES (9, 'Gestionnaire de paie', 2);
INSERT INTO metier (id, libelle, id_categorie) VALUES (10, 'Charge de recrutement', 2);

-- unite_orga (10)
INSERT INTO unite_orga (id, code, type, libelle, id_unite_mere, id_poste_responsable) VALUES (1, 'DG', 'DIRECTION', 'Direction generale', NULL, 1);
INSERT INTO unite_orga (id, code, type, libelle, id_unite_mere, id_poste_responsable) VALUES (2, 'DAF', 'DIRECTION', 'Direction financiere', 1, 2);
INSERT INTO unite_orga (id, code, type, libelle, id_unite_mere, id_poste_responsable) VALUES (3, 'DRH', 'DIRECTION', 'DRH', 1, 3);
INSERT INTO unite_orga (id, code, type, libelle, id_unite_mere, id_poste_responsable) VALUES (4, 'S-COMPTA', 'SERVICE', 'Service Compta', 2, 5);
INSERT INTO unite_orga (id, code, type, libelle, id_unite_mere, id_poste_responsable) VALUES (5, 'DSI', 'DIRECTION', 'Direction des systemes d''information', 1, 4);
INSERT INTO unite_orga (id, code, type, libelle, id_unite_mere, id_poste_responsable) VALUES (6, 'DEP-DEV', 'DEPARTEMENT', 'Departement Developpement', 5, 8);
INSERT INTO unite_orga (id, code, type, libelle, id_unite_mere, id_poste_responsable) VALUES (7, 'DEP-INFRA', 'DEPARTEMENT', 'Departement Infrastructure', 5, 12);
INSERT INTO unite_orga (id, code, type, libelle, id_unite_mere, id_poste_responsable) VALUES (8, 'S-RESEAU', 'SERVICE', 'Service Reseau', 7, 13);
INSERT INTO unite_orga (id, code, type, libelle, id_unite_mere, id_poste_responsable) VALUES (9, 'SEC-SUPPORT', 'SECTION', 'Section Support', 8, 14);
INSERT INTO unite_orga (id, code, type, libelle, id_unite_mere, id_poste_responsable) VALUES (10, 'S-PAIE', 'SERVICE', 'Service Paie', 3, 16);

-- poste (18)
INSERT INTO poste (id, code, id_metier, id_echelon, id_unite_orga, id_superieur_direct, libelle, etat, statut_occupation) VALUES (1, 'POST-001', 1, 3, 1, NULL, 'Directeur general', 'ACTIF', 'OCCUPE');
INSERT INTO poste (id, code, id_metier, id_echelon, id_unite_orga, id_superieur_direct, libelle, etat, statut_occupation) VALUES (2, 'POST-002', 2, 3, 2, 1, 'Directeur financier', 'ACTIF', 'OCCUPE');
INSERT INTO poste (id, code, id_metier, id_echelon, id_unite_orga, id_superieur_direct, libelle, etat, statut_occupation) VALUES (3, 'POST-003', 2, 3, 3, 1, 'Directeur des ressources humaines', 'ACTIF', 'OCCUPE');
INSERT INTO poste (id, code, id_metier, id_echelon, id_unite_orga, id_superieur_direct, libelle, etat, statut_occupation) VALUES (4, 'POST-004', 2, 3, 5, 1, 'Directeur des systemes d''information', 'ACTIF', 'OCCUPE');
INSERT INTO poste (id, code, id_metier, id_echelon, id_unite_orga, id_superieur_direct, libelle, etat, statut_occupation) VALUES (5, 'POST-005', 4, 3, 4, 2, 'Chef du service comptabilite', 'ACTIF', 'OCCUPE');
INSERT INTO poste (id, code, id_metier, id_echelon, id_unite_orga, id_superieur_direct, libelle, etat, statut_occupation) VALUES (6, 'POST-006', 5, 1, 4, 5, 'Comptable junior', 'ACTIF', 'OCCUPE');
INSERT INTO poste (id, code, id_metier, id_echelon, id_unite_orga, id_superieur_direct, libelle, etat, statut_occupation) VALUES (7, 'POST-007', 5, 2, 4, 5, 'Comptable confirme', 'ACTIF', 'OCCUPE');
INSERT INTO poste (id, code, id_metier, id_echelon, id_unite_orga, id_superieur_direct, libelle, etat, statut_occupation) VALUES (8, 'POST-008', 3, 3, 6, 4, 'Chef du departement developpement', 'ACTIF', 'OCCUPE');
INSERT INTO poste (id, code, id_metier, id_echelon, id_unite_orga, id_superieur_direct, libelle, etat, statut_occupation) VALUES (9, 'POST-009', 6, 1, 6, 8, 'Developpeur junior', 'ACTIF', 'OCCUPE');
INSERT INTO poste (id, code, id_metier, id_echelon, id_unite_orga, id_superieur_direct, libelle, etat, statut_occupation) VALUES (10, 'POST-010', 6, 2, 6, 8, 'Developpeur confirme', 'ACTIF', 'OCCUPE');
INSERT INTO poste (id, code, id_metier, id_echelon, id_unite_orga, id_superieur_direct, libelle, etat, statut_occupation) VALUES (11, 'POST-011', 6, 3, 6, 8, 'Developpeur senior', 'ACTIF', 'OCCUPE');
INSERT INTO poste (id, code, id_metier, id_echelon, id_unite_orga, id_superieur_direct, libelle, etat, statut_occupation) VALUES (12, 'POST-012', 3, 3, 7, 4, 'Chef du departement infrastructure', 'ACTIF', 'OCCUPE');
INSERT INTO poste (id, code, id_metier, id_echelon, id_unite_orga, id_superieur_direct, libelle, etat, statut_occupation) VALUES (13, 'POST-013', 4, 2, 8, 12, 'Chef du service reseau', 'ACTIF', 'OCCUPE');
INSERT INTO poste (id, code, id_metier, id_echelon, id_unite_orga, id_superieur_direct, libelle, etat, statut_occupation) VALUES (14, 'POST-014', 7, 2, 8, 13, 'Administrateur reseau', 'ACTIF', 'OCCUPE');
INSERT INTO poste (id, code, id_metier, id_echelon, id_unite_orga, id_superieur_direct, libelle, etat, statut_occupation) VALUES (15, 'POST-015', 8, 1, 9, 13, 'Technicien support', 'ACTIF', 'OCCUPE');
INSERT INTO poste (id, code, id_metier, id_echelon, id_unite_orga, id_superieur_direct, libelle, etat, statut_occupation) VALUES (16, 'POST-016', 4, 3, 10, 3, 'Chef du service paie', 'ACTIF', 'OCCUPE');
INSERT INTO poste (id, code, id_metier, id_echelon, id_unite_orga, id_superieur_direct, libelle, etat, statut_occupation) VALUES (17, 'POST-017', 9, 2, 10, 16, 'Gestionnaire de paie', 'ACTIF', 'OCCUPE');
INSERT INTO poste (id, code, id_metier, id_echelon, id_unite_orga, id_superieur_direct, libelle, etat, statut_occupation) VALUES (18, 'POST-018', 10, 2, 3, 3, 'Charge de recrutement', 'ACTIF', 'OCCUPE');

-- affectation (18)
INSERT INTO affectation (id, id_poste, id_employe, date_debut, date_fin) VALUES (1, 1, 1, '2026-01-02', NULL);
INSERT INTO affectation (id, id_poste, id_employe, date_debut, date_fin) VALUES (2, 2, 2, '2026-01-05', NULL);
INSERT INTO affectation (id, id_poste, id_employe, date_debut, date_fin) VALUES (3, 3, 3, '2026-01-05', NULL);
INSERT INTO affectation (id, id_poste, id_employe, date_debut, date_fin) VALUES (4, 4, 4, '2026-01-10', NULL);
INSERT INTO affectation (id, id_poste, id_employe, date_debut, date_fin) VALUES (5, 5, 5, '2026-01-12', NULL);
INSERT INTO affectation (id, id_poste, id_employe, date_debut, date_fin) VALUES (6, 6, 6, '2026-01-15', NULL);
INSERT INTO affectation (id, id_poste, id_employe, date_debut, date_fin) VALUES (7, 7, 7, '2026-01-15', NULL);
INSERT INTO affectation (id, id_poste, id_employe, date_debut, date_fin) VALUES (8, 8, 8, '2026-01-20', NULL);
INSERT INTO affectation (id, id_poste, id_employe, date_debut, date_fin) VALUES (9, 9, 9, '2026-01-22', NULL);
INSERT INTO affectation (id, id_poste, id_employe, date_debut, date_fin) VALUES (10, 10, 10, '2026-01-22', NULL);
INSERT INTO affectation (id, id_poste, id_employe, date_debut, date_fin) VALUES (11, 11, 11, '2026-01-25', NULL);
INSERT INTO affectation (id, id_poste, id_employe, date_debut, date_fin) VALUES (12, 12, 12, '2026-01-25', NULL);
INSERT INTO affectation (id, id_poste, id_employe, date_debut, date_fin) VALUES (13, 13, 13, '2026-02-01', NULL);
INSERT INTO affectation (id, id_poste, id_employe, date_debut, date_fin) VALUES (14, 14, 14, '2026-02-03', NULL);
INSERT INTO affectation (id, id_poste, id_employe, date_debut, date_fin) VALUES (15, 15, 15, '2026-02-03', NULL);
INSERT INTO affectation (id, id_poste, id_employe, date_debut, date_fin) VALUES (16, 16, 16, '2026-02-05', NULL);
INSERT INTO affectation (id, id_poste, id_employe, date_debut, date_fin) VALUES (17, 17, 17, '2026-02-05', NULL);
INSERT INTO affectation (id, id_poste, id_employe, date_debut, date_fin) VALUES (18, 18, 18, '2026-02-10', NULL);

-- motif (1)
INSERT INTO motif (id, libelle) VALUES (1, 'Test P5 - Surcroit d''activite');

-- user (20)
INSERT INTO "user" (id, login, mot_de_passe, id_employe, statut, email, nom_complet, date_creation, derniere_connexion) VALUES (1, 'manager.dev', 'hash_demo_manager_dev', 8, 'ACTIF', NULL, 'Mamy Razafimalala', '2026-10-23 14:11:21.248685', NULL);
INSERT INTO "user" (id, login, mot_de_passe, id_employe, statut, email, nom_complet, date_creation, derniere_connexion) VALUES (2, 'manager.compta', 'hash_demo_manager_compta', 5, 'ACTIF', NULL, 'Tojo Rakotondrabe', '2026-10-23 14:11:21.248685', NULL);
INSERT INTO "user" (id, login, mot_de_passe, id_employe, statut, email, nom_complet, date_creation, derniere_connexion) VALUES (3, 'rh.drh', 'hash_demo_rh', 18, 'ACTIF', NULL, 'Hasina Andrianjafy', '2026-10-23 14:11:21.248685', NULL);
INSERT INTO "user" (id, login, mot_de_passe, id_employe, statut, email, nom_complet, date_creation, derniere_connexion) VALUES (4, 'finance.daf', 'hash_demo_finance', 2, 'ACTIF', NULL, 'Sarah Randriamampianina', '2026-10-23 14:11:21.248685', NULL);
INSERT INTO "user" (id, login, mot_de_passe, id_employe, statut, email, nom_complet, date_creation, derniere_connexion) VALUES (5, 'dg', 'hash_demo_dg', 1, 'ACTIF', NULL, 'Jean Michel Rakoto', '2026-10-23 14:11:21.248685', NULL);
INSERT INTO "user" (id, login, mot_de_passe, id_employe, statut, email, nom_complet, date_creation, derniere_connexion) VALUES (6, 'admin', 'hash_demo_admin', NULL, 'ACTIF', NULL, 'Administrateur SIRH', '2026-10-23 14:11:21.2591', NULL);
INSERT INTO "user" (id, login, mot_de_passe, id_employe, statut, email, nom_complet, date_creation, derniere_connexion) VALUES (7, 'systeme', '!', NULL, 'DESACTIVE', NULL, 'Contrôle automatique', '2026-10-23 14:11:21.260733', NULL);
INSERT INTO "user" (id, login, mot_de_passe, id_employe, statut, email, nom_complet, date_creation, derniere_connexion) VALUES (8, 'paie', 'hash_demo_paie', 17, 'ACTIF', NULL, 'Lanto Rakotoniaina', '2026-10-23 14:11:21.261508', NULL);
INSERT INTO "user" (id, login, mot_de_passe, id_employe, statut, email, nom_complet, date_creation, derniere_connexion) VALUES (9, 'support', 'hash_demo_support', 15, 'ACTIF', NULL, 'Aina Razanadrakoto', '2026-10-23 14:11:21.262802', NULL);
INSERT INTO "user" (id, login, mot_de_passe, id_employe, statut, email, nom_complet, date_creation, derniere_connexion) VALUES (10, 'dev.senior', 'hash_demo_dev_senior', 11, 'ACTIF', NULL, 'Tsiry Razafindraibe', '2026-10-23 14:11:21.919815', NULL);
INSERT INTO "user" (id, login, mot_de_passe, id_employe, statut, email, nom_complet, date_creation, derniere_connexion) VALUES (11, 'chef.infra', 'hash_demo_chef_infra', 12, 'ACTIF', NULL, 'Nirina Rabary', '2026-10-23 14:11:21.919815', NULL);
INSERT INTO "user" (id, login, mot_de_passe, id_employe, statut, email, nom_complet, date_creation, derniere_connexion) VALUES (12, 'dev.confirme', 'hash_demo_dev_confirme', 10, 'ACTIF', NULL, 'Tiana Rakotomalala', '2026-10-23 14:11:21.919815', NULL);
INSERT INTO "user" (id, login, mot_de_passe, id_employe, statut, email, nom_complet, date_creation, derniere_connexion) VALUES (13, 'chef.reseau', 'hash_demo_chef_reseau', 13, 'ACTIF', NULL, 'Fara Andriambololona', '2026-10-23 14:11:21.919815', NULL);
INSERT INTO "user" (id, login, mot_de_passe, id_employe, statut, email, nom_complet, date_creation, derniere_connexion) VALUES (14, 'compta.junior', 'hash_demo_compta_junior', 6, 'ACTIF', NULL, 'Feno Andrianasolo', '2026-10-23 14:11:21.919815', NULL);
INSERT INTO "user" (id, login, mot_de_passe, id_employe, statut, email, nom_complet, date_creation, derniere_connexion) VALUES (15, 'chef.paie', 'hash_demo_chef_paie', 16, 'ACTIF', NULL, 'Voahirana Randrianantenaina', '2026-10-23 14:11:21.919815', NULL);
INSERT INTO "user" (id, login, mot_de_passe, id_employe, statut, email, nom_complet, date_creation, derniere_connexion) VALUES (16, 'dsi', 'hash_demo_dsi', 4, 'ACTIF', NULL, 'Miora Rabemananjara', '2026-10-23 14:11:21.919815', NULL);
INSERT INTO "user" (id, login, mot_de_passe, id_employe, statut, email, nom_complet, date_creation, derniere_connexion) VALUES (17, 'drh', 'hash_demo_drh', 3, 'ACTIF', NULL, 'Hery Razafindrakoto', '2026-10-23 14:11:21.919815', NULL);
INSERT INTO "user" (id, login, mot_de_passe, id_employe, statut, email, nom_complet, date_creation, derniere_connexion) VALUES (18, 'admin.reseau', 'hash_demo_admin_reseau', 14, 'ACTIF', NULL, 'Haja Rakotoarisoa', '2026-10-23 14:11:21.919815', NULL);
INSERT INTO "user" (id, login, mot_de_passe, id_employe, statut, email, nom_complet, date_creation, derniere_connexion) VALUES (19, 'dev.junior', 'hash_demo_dev_junior', 9, 'ACTIF', NULL, 'Lova Randrianarisoa', '2026-10-23 14:11:21.919815', NULL);
INSERT INTO "user" (id, login, mot_de_passe, id_employe, statut, email, nom_complet, date_creation, derniere_connexion) VALUES (20, 'compta.confirme', 'hash_demo_compta_confirme', 7, 'ACTIF', NULL, 'Nantenaina Rasolofoarisoa', '2026-10-23 14:11:21.919815', NULL);

-- besoin (5)
INSERT INTO besoin (id, reference, type_besoin, id_motif, id_unite_orga, date_souhaitee, id_user, effectif, etat_actuel, budget_estime, precision_motif) VALUES (1, 'BES-TEST-P5-1', 'CREATION', 1, 4, '2026-12-07', 3, 2, 'OUVERT', NULL, NULL);
INSERT INTO besoin (id, reference, type_besoin, id_motif, id_unite_orga, date_souhaitee, id_user, effectif, etat_actuel, budget_estime, precision_motif) VALUES (2, 'BES-TEST-P5-2', 'CREATION', 1, 6, '2026-12-07', 3, 1, 'OUVERT', NULL, NULL);
INSERT INTO besoin (id, reference, type_besoin, id_motif, id_unite_orga, date_souhaitee, id_user, effectif, etat_actuel, budget_estime, precision_motif) VALUES (3, 'BES-TEST-P5-3', 'CREATION', 1, 8, '2026-12-07', 3, 1, 'OUVERT', NULL, NULL);
INSERT INTO besoin (id, reference, type_besoin, id_motif, id_unite_orga, date_souhaitee, id_user, effectif, etat_actuel, budget_estime, precision_motif) VALUES (4, 'BES-TEST-P5-4', 'CREATION', 1, 10, '2026-12-07', 3, 1, 'OUVERT', NULL, NULL);
INSERT INTO besoin (id, reference, type_besoin, id_motif, id_unite_orga, date_souhaitee, id_user, effectif, etat_actuel, budget_estime, precision_motif) VALUES (5, 'BES-TEST-P5-5', 'CREATION', 1, 6, '2026-12-07', 3, 1, 'OUVERT', NULL, NULL);

-- candidat (3)
INSERT INTO candidat (id, nom, prenom, date_naissance, adresse, email, numero, id_personne) VALUES (1, 'Rakoto', 'Paul', '1993-11-20', NULL, 'rakoto.test@mail.mg', '0343333333', 1);
INSERT INTO candidat (id, nom, prenom, date_naissance, adresse, email, numero, id_personne) VALUES (2, 'Rasoa', 'Marie', '1998-07-15', NULL, 'rasoa.test@mail.mg', '0342222222', 2);
INSERT INTO candidat (id, nom, prenom, date_naissance, adresse, email, numero, id_personne) VALUES (3, 'Rabe', 'Jean', '1995-03-02', NULL, 'rabe.test@mail.mg', '0341111111', 3);

-- type_document (12)
INSERT INTO type_document (id, libelle) VALUES (1, 'CV');
INSERT INTO type_document (id, libelle) VALUES (2, 'LETTRE_MOTIVATION');
INSERT INTO type_document (id, libelle) VALUES (3, 'DIPLOME');
INSERT INTO type_document (id, libelle) VALUES (4, 'ATTESTATION');
INSERT INTO type_document (id, libelle) VALUES (5, 'PIECE_IDENTITE');
INSERT INTO type_document (id, libelle) VALUES (6, 'PHOTO');
INSERT INTO type_document (id, libelle) VALUES (7, 'OFFRE');
INSERT INTO type_document (id, libelle) VALUES (8, 'CONTRAT');
INSERT INTO type_document (id, libelle) VALUES (9, 'FICHE_POSTE');
INSERT INTO type_document (id, libelle) VALUES (10, 'RAPPORT');
INSERT INTO type_document (id, libelle) VALUES (11, 'AUTRE');
INSERT INTO type_document (id, libelle) VALUES (12, 'CONTRAT_SIGNE');

-- fiche_poste (10)
INSERT INTO fiche_poste (id, id_metier, id_poste, mission, activites, formation, experience, competences, conditions, version, date_maj, id_auteur) VALUES (1, 1, NULL, 'Contribuer, en tant que Directeur general, aux objectifs de son unité et de l''entreprise, dans le respect des procédures et des délais fixés.', 'Réaliser les tâches propres au métier de Directeur general.
Rendre compte régulièrement de son activité à son supérieur direct.
Respecter les procédures internes, la confidentialité et les règles de sécurité.
Collaborer avec les autres équipes et partager l''information utile.', 'Diplôme ou qualification en rapport avec le métier de Directeur general.', 'Expérience adaptée au niveau du poste (voir l''échelon).', 'Maîtrise des techniques du métier.
Sens de l''organisation et de la rigueur.
Capacité de travail en équipe et bonne communication.', 'Temps plein, 40 heures par semaine.
Lieu de travail : siège de l''entreprise.', 1, '2026-10-23 14:11:21.750385', NULL);
INSERT INTO fiche_poste (id, id_metier, id_poste, mission, activites, formation, experience, competences, conditions, version, date_maj, id_auteur) VALUES (2, 2, NULL, 'Contribuer, en tant que Directeur, aux objectifs de son unité et de l''entreprise, dans le respect des procédures et des délais fixés.', 'Réaliser les tâches propres au métier de Directeur.
Rendre compte régulièrement de son activité à son supérieur direct.
Respecter les procédures internes, la confidentialité et les règles de sécurité.
Collaborer avec les autres équipes et partager l''information utile.', 'Diplôme ou qualification en rapport avec le métier de Directeur.', 'Expérience adaptée au niveau du poste (voir l''échelon).', 'Maîtrise des techniques du métier.
Sens de l''organisation et de la rigueur.
Capacité de travail en équipe et bonne communication.', 'Temps plein, 40 heures par semaine.
Lieu de travail : siège de l''entreprise.', 1, '2026-10-23 14:11:21.750385', NULL);
INSERT INTO fiche_poste (id, id_metier, id_poste, mission, activites, formation, experience, competences, conditions, version, date_maj, id_auteur) VALUES (3, 3, NULL, 'Contribuer, en tant que Chef de departement, aux objectifs de son unité et de l''entreprise, dans le respect des procédures et des délais fixés.', 'Réaliser les tâches propres au métier de Chef de departement.
Rendre compte régulièrement de son activité à son supérieur direct.
Respecter les procédures internes, la confidentialité et les règles de sécurité.
Collaborer avec les autres équipes et partager l''information utile.', 'Diplôme ou qualification en rapport avec le métier de Chef de departement.', 'Expérience adaptée au niveau du poste (voir l''échelon).', 'Maîtrise des techniques du métier.
Sens de l''organisation et de la rigueur.
Capacité de travail en équipe et bonne communication.', 'Temps plein, 40 heures par semaine.
Lieu de travail : siège de l''entreprise.', 1, '2026-10-23 14:11:21.750385', NULL);
INSERT INTO fiche_poste (id, id_metier, id_poste, mission, activites, formation, experience, competences, conditions, version, date_maj, id_auteur) VALUES (4, 4, NULL, 'Contribuer, en tant que Chef de service, aux objectifs de son unité et de l''entreprise, dans le respect des procédures et des délais fixés.', 'Réaliser les tâches propres au métier de Chef de service.
Rendre compte régulièrement de son activité à son supérieur direct.
Respecter les procédures internes, la confidentialité et les règles de sécurité.
Collaborer avec les autres équipes et partager l''information utile.', 'Diplôme ou qualification en rapport avec le métier de Chef de service.', 'Expérience adaptée au niveau du poste (voir l''échelon).', 'Maîtrise des techniques du métier.
Sens de l''organisation et de la rigueur.
Capacité de travail en équipe et bonne communication.', 'Temps plein, 40 heures par semaine.
Lieu de travail : siège de l''entreprise.', 1, '2026-10-23 14:11:21.750385', NULL);
INSERT INTO fiche_poste (id, id_metier, id_poste, mission, activites, formation, experience, competences, conditions, version, date_maj, id_auteur) VALUES (5, 5, NULL, 'Contribuer, en tant que Comptable, aux objectifs de son unité et de l''entreprise, dans le respect des procédures et des délais fixés.', 'Réaliser les tâches propres au métier de Comptable.
Rendre compte régulièrement de son activité à son supérieur direct.
Respecter les procédures internes, la confidentialité et les règles de sécurité.
Collaborer avec les autres équipes et partager l''information utile.', 'Diplôme ou qualification en rapport avec le métier de Comptable.', 'Expérience adaptée au niveau du poste (voir l''échelon).', 'Maîtrise des techniques du métier.
Sens de l''organisation et de la rigueur.
Capacité de travail en équipe et bonne communication.', 'Temps plein, 40 heures par semaine.
Lieu de travail : siège de l''entreprise.', 1, '2026-10-23 14:11:21.750385', NULL);
INSERT INTO fiche_poste (id, id_metier, id_poste, mission, activites, formation, experience, competences, conditions, version, date_maj, id_auteur) VALUES (6, 6, NULL, 'Contribuer, en tant que Developpeur, aux objectifs de son unité et de l''entreprise, dans le respect des procédures et des délais fixés.', 'Réaliser les tâches propres au métier de Developpeur.
Rendre compte régulièrement de son activité à son supérieur direct.
Respecter les procédures internes, la confidentialité et les règles de sécurité.
Collaborer avec les autres équipes et partager l''information utile.', 'Diplôme ou qualification en rapport avec le métier de Developpeur.', 'Expérience adaptée au niveau du poste (voir l''échelon).', 'Maîtrise des techniques du métier.
Sens de l''organisation et de la rigueur.
Capacité de travail en équipe et bonne communication.', 'Temps plein, 40 heures par semaine.
Lieu de travail : siège de l''entreprise.', 1, '2026-10-23 14:11:21.750385', NULL);
INSERT INTO fiche_poste (id, id_metier, id_poste, mission, activites, formation, experience, competences, conditions, version, date_maj, id_auteur) VALUES (7, 7, NULL, 'Contribuer, en tant que Administrateur reseau, aux objectifs de son unité et de l''entreprise, dans le respect des procédures et des délais fixés.', 'Réaliser les tâches propres au métier de Administrateur reseau.
Rendre compte régulièrement de son activité à son supérieur direct.
Respecter les procédures internes, la confidentialité et les règles de sécurité.
Collaborer avec les autres équipes et partager l''information utile.', 'Diplôme ou qualification en rapport avec le métier de Administrateur reseau.', 'Expérience adaptée au niveau du poste (voir l''échelon).', 'Maîtrise des techniques du métier.
Sens de l''organisation et de la rigueur.
Capacité de travail en équipe et bonne communication.', 'Temps plein, 40 heures par semaine.
Lieu de travail : siège de l''entreprise.', 1, '2026-10-23 14:11:21.750385', NULL);
INSERT INTO fiche_poste (id, id_metier, id_poste, mission, activites, formation, experience, competences, conditions, version, date_maj, id_auteur) VALUES (8, 8, NULL, 'Contribuer, en tant que Technicien support, aux objectifs de son unité et de l''entreprise, dans le respect des procédures et des délais fixés.', 'Réaliser les tâches propres au métier de Technicien support.
Rendre compte régulièrement de son activité à son supérieur direct.
Respecter les procédures internes, la confidentialité et les règles de sécurité.
Collaborer avec les autres équipes et partager l''information utile.', 'Diplôme ou qualification en rapport avec le métier de Technicien support.', 'Expérience adaptée au niveau du poste (voir l''échelon).', 'Maîtrise des techniques du métier.
Sens de l''organisation et de la rigueur.
Capacité de travail en équipe et bonne communication.', 'Temps plein, 40 heures par semaine.
Lieu de travail : siège de l''entreprise.', 1, '2026-10-23 14:11:21.750385', NULL);
INSERT INTO fiche_poste (id, id_metier, id_poste, mission, activites, formation, experience, competences, conditions, version, date_maj, id_auteur) VALUES (9, 9, NULL, 'Contribuer, en tant que Gestionnaire de paie, aux objectifs de son unité et de l''entreprise, dans le respect des procédures et des délais fixés.', 'Réaliser les tâches propres au métier de Gestionnaire de paie.
Rendre compte régulièrement de son activité à son supérieur direct.
Respecter les procédures internes, la confidentialité et les règles de sécurité.
Collaborer avec les autres équipes et partager l''information utile.', 'Diplôme ou qualification en rapport avec le métier de Gestionnaire de paie.', 'Expérience adaptée au niveau du poste (voir l''échelon).', 'Maîtrise des techniques du métier.
Sens de l''organisation et de la rigueur.
Capacité de travail en équipe et bonne communication.', 'Temps plein, 40 heures par semaine.
Lieu de travail : siège de l''entreprise.', 1, '2026-10-23 14:11:21.750385', NULL);
INSERT INTO fiche_poste (id, id_metier, id_poste, mission, activites, formation, experience, competences, conditions, version, date_maj, id_auteur) VALUES (10, 10, NULL, 'Contribuer, en tant que Charge de recrutement, aux objectifs de son unité et de l''entreprise, dans le respect des procédures et des délais fixés.', 'Réaliser les tâches propres au métier de Charge de recrutement.
Rendre compte régulièrement de son activité à son supérieur direct.
Respecter les procédures internes, la confidentialité et les règles de sécurité.
Collaborer avec les autres équipes et partager l''information utile.', 'Diplôme ou qualification en rapport avec le métier de Charge de recrutement.', 'Expérience adaptée au niveau du poste (voir l''échelon).', 'Maîtrise des techniques du métier.
Sens de l''organisation et de la rigueur.
Capacité de travail en équipe et bonne communication.', 'Temps plein, 40 heures par semaine.
Lieu de travail : siège de l''entreprise.', 1, '2026-10-23 14:11:21.750385', NULL);

-- type_contrat (3)
INSERT INTO type_contrat (id, libelle, duree_max, renouvellement_max) VALUES (2, 'CDI', NULL, NULL);
INSERT INTO type_contrat (id, libelle, duree_max, renouvellement_max) VALUES (1, 'CE', 6, 2);
INSERT INTO type_contrat (id, libelle, duree_max, renouvellement_max) VALUES (3, 'CDD', 24, 1);

-- offre_emploi (6)
INSERT INTO offre_emploi (id, id_besoin, ref, id_type_contrat, date_publication, date_limite_candidature, id_document, etat, date_cloture, motif_cloture, id_user_cloture, id_poste, id_fiche_poste) VALUES (2, 2, 'OFF-TEST-P5-2', NULL, '2026-10-21', '2026-10-23', NULL, 'PUBLIEE', NULL, NULL, NULL, 9, NULL);
INSERT INTO offre_emploi (id, id_besoin, ref, id_type_contrat, date_publication, date_limite_candidature, id_document, etat, date_cloture, motif_cloture, id_user_cloture, id_poste, id_fiche_poste) VALUES (3, 3, 'OFF-TEST-P5-3', 2, '2026-09-23', '2026-10-22', NULL, 'PUBLIEE', NULL, NULL, NULL, 14, NULL);
INSERT INTO offre_emploi (id, id_besoin, ref, id_type_contrat, date_publication, date_limite_candidature, id_document, etat, date_cloture, motif_cloture, id_user_cloture, id_poste, id_fiche_poste) VALUES (4, 4, 'OFF-TEST-P5-4', 3, NULL, '2026-11-02', NULL, 'BROUILLON', NULL, NULL, NULL, 17, NULL);
INSERT INTO offre_emploi (id, id_besoin, ref, id_type_contrat, date_publication, date_limite_candidature, id_document, etat, date_cloture, motif_cloture, id_user_cloture, id_poste, id_fiche_poste) VALUES (5, 5, 'OFF-TEST-P5-5', 3, '2026-10-26', '2026-11-22', NULL, 'PUBLIEE', NULL, NULL, NULL, 10, NULL);
INSERT INTO offre_emploi (id, id_besoin, ref, id_type_contrat, date_publication, date_limite_candidature, id_document, etat, date_cloture, motif_cloture, id_user_cloture, id_poste, id_fiche_poste) VALUES (6, 1, 'OFF-TEST-P5-1-2', 1, '2026-10-18', '2026-11-12', NULL, 'PUBLIEE', NULL, NULL, NULL, 7, NULL);
INSERT INTO offre_emploi (id, id_besoin, ref, id_type_contrat, date_publication, date_limite_candidature, id_document, etat, date_cloture, motif_cloture, id_user_cloture, id_poste, id_fiche_poste) VALUES (1, 1, 'OFF-TEST-P5-1', 1, '2026-10-18', '2026-11-12', NULL, 'PUBLIEE', NULL, NULL, NULL, 6, NULL);

-- candidatures (3)
INSERT INTO candidatures (id, id_candidat, id_offre_emploi, date_depot, etat) VALUES (1, 1, 1, '2026-10-21', 'DEPOSEE');
INSERT INTO candidatures (id, id_candidat, id_offre_emploi, date_depot, etat) VALUES (2, 2, 1, '2026-10-21', 'DEPOSEE');
INSERT INTO candidatures (id, id_candidat, id_offre_emploi, date_depot, etat) VALUES (3, 3, 1, '2026-10-21', 'DEPOSEE');

-- besoin_poste (6)
INSERT INTO besoin_poste (id, id_besoin, id_poste, etat, id_candidature_retenue, id_employe_remplace) VALUES (1, 1, 6, 'OUVERT', NULL, NULL);
INSERT INTO besoin_poste (id, id_besoin, id_poste, etat, id_candidature_retenue, id_employe_remplace) VALUES (2, 3, 14, 'OUVERT', NULL, NULL);
INSERT INTO besoin_poste (id, id_besoin, id_poste, etat, id_candidature_retenue, id_employe_remplace) VALUES (3, 1, 7, 'OUVERT', NULL, NULL);
INSERT INTO besoin_poste (id, id_besoin, id_poste, etat, id_candidature_retenue, id_employe_remplace) VALUES (4, 2, 9, 'OUVERT', NULL, NULL);
INSERT INTO besoin_poste (id, id_besoin, id_poste, etat, id_candidature_retenue, id_employe_remplace) VALUES (5, 5, 10, 'OUVERT', NULL, NULL);
INSERT INTO besoin_poste (id, id_besoin, id_poste, etat, id_candidature_retenue, id_employe_remplace) VALUES (6, 4, 17, 'OUVERT', NULL, NULL);

-- budget_unite (10)
INSERT INTO budget_unite (id, id_unite_orga, exercice, montant, montant_propose, statut, id_user_propose, id_user_decision, date_decision, commentaire) VALUES (1, 1, 2026, 500000000.00, NULL, 'VALIDE', NULL, NULL, NULL, NULL);
INSERT INTO budget_unite (id, id_unite_orga, exercice, montant, montant_propose, statut, id_user_propose, id_user_decision, date_decision, commentaire) VALUES (5, 5, 2026, 200000000.00, NULL, 'VALIDE', NULL, NULL, NULL, NULL);
INSERT INTO budget_unite (id, id_unite_orga, exercice, montant, montant_propose, statut, id_user_propose, id_user_decision, date_decision, commentaire) VALUES (9, 9, 2026, 25000000.00, NULL, 'VALIDE', NULL, NULL, NULL, NULL);
INSERT INTO budget_unite (id, id_unite_orga, exercice, montant, montant_propose, statut, id_user_propose, id_user_decision, date_decision, commentaire) VALUES (2, 2, 2026, 130000000.00, NULL, 'VALIDE', NULL, NULL, NULL, NULL);
INSERT INTO budget_unite (id, id_unite_orga, exercice, montant, montant_propose, statut, id_user_propose, id_user_decision, date_decision, commentaire) VALUES (3, 3, 2026, 180000000.00, NULL, 'VALIDE', NULL, NULL, NULL, NULL);
INSERT INTO budget_unite (id, id_unite_orga, exercice, montant, montant_propose, statut, id_user_propose, id_user_decision, date_decision, commentaire) VALUES (4, 4, 2026, 130000000.00, NULL, 'VALIDE', NULL, NULL, NULL, NULL);
INSERT INTO budget_unite (id, id_unite_orga, exercice, montant, montant_propose, statut, id_user_propose, id_user_decision, date_decision, commentaire) VALUES (6, 6, 2026, 260000000.00, NULL, 'VALIDE', NULL, NULL, NULL, NULL);
INSERT INTO budget_unite (id, id_unite_orga, exercice, montant, montant_propose, statut, id_user_propose, id_user_decision, date_decision, commentaire) VALUES (7, 7, 2026, 130000000.00, NULL, 'VALIDE', NULL, NULL, NULL, NULL);
INSERT INTO budget_unite (id, id_unite_orga, exercice, montant, montant_propose, statut, id_user_propose, id_user_decision, date_decision, commentaire) VALUES (8, 8, 2026, 90000000.00, NULL, 'VALIDE', NULL, NULL, NULL, NULL);
INSERT INTO budget_unite (id, id_unite_orga, exercice, montant, montant_propose, statut, id_user_propose, id_user_decision, date_decision, commentaire) VALUES (10, 10, 2026, 100000000.00, NULL, 'VALIDE', NULL, NULL, NULL, NULL);

-- type_information (3)
INSERT INTO type_information (id, libelle) VALUES (1, 'EXPERIENCE PRO');
INSERT INTO type_information (id, libelle) VALUES (2, 'EDUCATION');
INSERT INTO type_information (id, libelle) VALUES (3, 'QUALITE');

-- contrat (18)
INSERT INTO contrat (id, reference, id_affectation, id_proposition, id_type, date_debut, date_fin_prevue, date_fin_effective, statut_actuel, id_contrat_precedent, id_document, motif_fin) VALUES (1, 'CTR-REP-EMP-2026-001', 1, NULL, 2, '2026-01-02', NULL, NULL, 'SIGNE', NULL, NULL, NULL);
INSERT INTO contrat (id, reference, id_affectation, id_proposition, id_type, date_debut, date_fin_prevue, date_fin_effective, statut_actuel, id_contrat_precedent, id_document, motif_fin) VALUES (2, 'CTR-REP-EMP-2026-002', 2, NULL, 2, '2026-01-05', NULL, NULL, 'SIGNE', NULL, NULL, NULL);
INSERT INTO contrat (id, reference, id_affectation, id_proposition, id_type, date_debut, date_fin_prevue, date_fin_effective, statut_actuel, id_contrat_precedent, id_document, motif_fin) VALUES (3, 'CTR-REP-EMP-2026-003', 3, NULL, 2, '2026-01-05', NULL, NULL, 'SIGNE', NULL, NULL, NULL);
INSERT INTO contrat (id, reference, id_affectation, id_proposition, id_type, date_debut, date_fin_prevue, date_fin_effective, statut_actuel, id_contrat_precedent, id_document, motif_fin) VALUES (4, 'CTR-REP-EMP-2026-004', 4, NULL, 2, '2026-01-10', NULL, NULL, 'SIGNE', NULL, NULL, NULL);
INSERT INTO contrat (id, reference, id_affectation, id_proposition, id_type, date_debut, date_fin_prevue, date_fin_effective, statut_actuel, id_contrat_precedent, id_document, motif_fin) VALUES (5, 'CTR-REP-EMP-2026-005', 5, NULL, 2, '2026-01-12', NULL, NULL, 'SIGNE', NULL, NULL, NULL);
INSERT INTO contrat (id, reference, id_affectation, id_proposition, id_type, date_debut, date_fin_prevue, date_fin_effective, statut_actuel, id_contrat_precedent, id_document, motif_fin) VALUES (6, 'CTR-REP-EMP-2026-006', 6, NULL, 3, '2026-01-15', '2028-01-14', NULL, 'SIGNE', NULL, NULL, NULL);
INSERT INTO contrat (id, reference, id_affectation, id_proposition, id_type, date_debut, date_fin_prevue, date_fin_effective, statut_actuel, id_contrat_precedent, id_document, motif_fin) VALUES (7, 'CTR-REP-EMP-2026-007', 7, NULL, 2, '2026-01-15', NULL, NULL, 'SIGNE', NULL, NULL, NULL);
INSERT INTO contrat (id, reference, id_affectation, id_proposition, id_type, date_debut, date_fin_prevue, date_fin_effective, statut_actuel, id_contrat_precedent, id_document, motif_fin) VALUES (8, 'CTR-REP-EMP-2026-008', 8, NULL, 2, '2026-01-20', NULL, NULL, 'SIGNE', NULL, NULL, NULL);
INSERT INTO contrat (id, reference, id_affectation, id_proposition, id_type, date_debut, date_fin_prevue, date_fin_effective, statut_actuel, id_contrat_precedent, id_document, motif_fin) VALUES (9, 'CTR-REP-EMP-2026-009', 9, NULL, 3, '2026-01-22', '2028-01-21', NULL, 'SIGNE', NULL, NULL, NULL);
INSERT INTO contrat (id, reference, id_affectation, id_proposition, id_type, date_debut, date_fin_prevue, date_fin_effective, statut_actuel, id_contrat_precedent, id_document, motif_fin) VALUES (10, 'CTR-REP-EMP-2026-010', 10, NULL, 2, '2026-01-22', NULL, NULL, 'SIGNE', NULL, NULL, NULL);
INSERT INTO contrat (id, reference, id_affectation, id_proposition, id_type, date_debut, date_fin_prevue, date_fin_effective, statut_actuel, id_contrat_precedent, id_document, motif_fin) VALUES (11, 'CTR-REP-EMP-2026-011', 11, NULL, 2, '2026-01-25', NULL, NULL, 'SIGNE', NULL, NULL, NULL);
INSERT INTO contrat (id, reference, id_affectation, id_proposition, id_type, date_debut, date_fin_prevue, date_fin_effective, statut_actuel, id_contrat_precedent, id_document, motif_fin) VALUES (12, 'CTR-REP-EMP-2026-012', 12, NULL, 2, '2026-01-25', NULL, NULL, 'SIGNE', NULL, NULL, NULL);
INSERT INTO contrat (id, reference, id_affectation, id_proposition, id_type, date_debut, date_fin_prevue, date_fin_effective, statut_actuel, id_contrat_precedent, id_document, motif_fin) VALUES (13, 'CTR-REP-EMP-2026-013', 13, NULL, 2, '2026-02-01', NULL, NULL, 'SIGNE', NULL, NULL, NULL);
INSERT INTO contrat (id, reference, id_affectation, id_proposition, id_type, date_debut, date_fin_prevue, date_fin_effective, statut_actuel, id_contrat_precedent, id_document, motif_fin) VALUES (14, 'CTR-REP-EMP-2026-014', 14, NULL, 2, '2026-02-03', NULL, NULL, 'SIGNE', NULL, NULL, NULL);
INSERT INTO contrat (id, reference, id_affectation, id_proposition, id_type, date_debut, date_fin_prevue, date_fin_effective, statut_actuel, id_contrat_precedent, id_document, motif_fin) VALUES (15, 'CTR-REP-EMP-2026-015', 15, NULL, 3, '2026-02-03', '2028-02-02', NULL, 'SIGNE', NULL, NULL, NULL);
INSERT INTO contrat (id, reference, id_affectation, id_proposition, id_type, date_debut, date_fin_prevue, date_fin_effective, statut_actuel, id_contrat_precedent, id_document, motif_fin) VALUES (16, 'CTR-REP-EMP-2026-016', 16, NULL, 2, '2026-02-05', NULL, NULL, 'SIGNE', NULL, NULL, NULL);
INSERT INTO contrat (id, reference, id_affectation, id_proposition, id_type, date_debut, date_fin_prevue, date_fin_effective, statut_actuel, id_contrat_precedent, id_document, motif_fin) VALUES (17, 'CTR-REP-EMP-2026-017', 17, NULL, 2, '2026-02-05', NULL, NULL, 'SIGNE', NULL, NULL, NULL);
INSERT INTO contrat (id, reference, id_affectation, id_proposition, id_type, date_debut, date_fin_prevue, date_fin_effective, statut_actuel, id_contrat_precedent, id_document, motif_fin) VALUES (18, 'CTR-REP-EMP-2026-018', 18, NULL, 2, '2026-02-10', NULL, NULL, 'SIGNE', NULL, NULL, NULL);

-- point_condition (6)
INSERT INTO point_condition (id, libelle) VALUES (1, 'SALAIRE');
INSERT INTO point_condition (id, libelle) VALUES (2, 'DATE_DEBUT');
INSERT INTO point_condition (id, libelle) VALUES (3, 'HORAIRE');
INSERT INTO point_condition (id, libelle) VALUES (4, 'LIEU');
INSERT INTO point_condition (id, libelle) VALUES (5, 'CONDITIONS');
INSERT INTO point_condition (id, libelle) VALUES (6, 'TYPE_CONTRAT');

-- contrat_remuneration (18)
INSERT INTO contrat_remuneration (id, id_contrat, date_effet, salaire, id_echelon) VALUES (1, 11, '2026-01-25', 3400000.00, 3);
INSERT INTO contrat_remuneration (id, id_contrat, date_effet, salaire, id_echelon) VALUES (2, 17, '2026-02-05', 2500000.00, 2);
INSERT INTO contrat_remuneration (id, id_contrat, date_effet, salaire, id_echelon) VALUES (3, 12, '2026-01-25', 8000000.00, 3);
INSERT INTO contrat_remuneration (id, id_contrat, date_effet, salaire, id_echelon) VALUES (4, 10, '2026-01-22', 2300000.00, 2);
INSERT INTO contrat_remuneration (id, id_contrat, date_effet, salaire, id_echelon) VALUES (5, 18, '2026-02-10', 2600000.00, 2);
INSERT INTO contrat_remuneration (id, id_contrat, date_effet, salaire, id_echelon) VALUES (6, 2, '2026-01-05', 8000000.00, 3);
INSERT INTO contrat_remuneration (id, id_contrat, date_effet, salaire, id_echelon) VALUES (7, 15, '2026-02-03', 850000.00, 1);
INSERT INTO contrat_remuneration (id, id_contrat, date_effet, salaire, id_echelon) VALUES (8, 13, '2026-02-01', 2600000.00, 2);
INSERT INTO contrat_remuneration (id, id_contrat, date_effet, salaire, id_echelon) VALUES (9, 5, '2026-01-12', 3300000.00, 3);
INSERT INTO contrat_remuneration (id, id_contrat, date_effet, salaire, id_echelon) VALUES (10, 8, '2026-01-20', 8400000.00, 3);
INSERT INTO contrat_remuneration (id, id_contrat, date_effet, salaire, id_echelon) VALUES (11, 6, '2026-01-15', 1700000.00, 1);
INSERT INTO contrat_remuneration (id, id_contrat, date_effet, salaire, id_echelon) VALUES (12, 16, '2026-02-05', 3400000.00, 3);
INSERT INTO contrat_remuneration (id, id_contrat, date_effet, salaire, id_echelon) VALUES (13, 4, '2026-01-10', 8800000.00, 3);
INSERT INTO contrat_remuneration (id, id_contrat, date_effet, salaire, id_echelon) VALUES (14, 1, '2026-01-02', 7600000.00, 3);
INSERT INTO contrat_remuneration (id, id_contrat, date_effet, salaire, id_echelon) VALUES (15, 3, '2026-01-05', 8400000.00, 3);
INSERT INTO contrat_remuneration (id, id_contrat, date_effet, salaire, id_echelon) VALUES (16, 14, '2026-02-03', 2700000.00, 2);
INSERT INTO contrat_remuneration (id, id_contrat, date_effet, salaire, id_echelon) VALUES (17, 9, '2026-01-22', 1850000.00, 1);
INSERT INTO contrat_remuneration (id, id_contrat, date_effet, salaire, id_echelon) VALUES (18, 7, '2026-01-15', 2500000.00, 2);

-- etat_contrat (54)
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (1, 11, 'CREE', '2026-01-25 08:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (2, 11, 'VALIDE', '2026-01-25 09:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (3, 11, 'SIGNE', '2026-01-25 10:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (4, 17, 'CREE', '2026-02-05 08:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (5, 17, 'VALIDE', '2026-02-05 09:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (6, 17, 'SIGNE', '2026-02-05 10:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (7, 12, 'CREE', '2026-01-25 08:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (8, 12, 'VALIDE', '2026-01-25 09:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (9, 12, 'SIGNE', '2026-01-25 10:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (10, 10, 'CREE', '2026-01-22 08:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (11, 10, 'VALIDE', '2026-01-22 09:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (12, 10, 'SIGNE', '2026-01-22 10:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (13, 18, 'CREE', '2026-02-10 08:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (14, 18, 'VALIDE', '2026-02-10 09:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (15, 18, 'SIGNE', '2026-02-10 10:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (16, 2, 'CREE', '2026-01-05 08:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (17, 2, 'VALIDE', '2026-01-05 09:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (18, 2, 'SIGNE', '2026-01-05 10:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (19, 15, 'CREE', '2026-02-03 08:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (20, 15, 'VALIDE', '2026-02-03 09:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (21, 15, 'SIGNE', '2026-02-03 10:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (22, 13, 'CREE', '2026-02-01 08:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (23, 13, 'VALIDE', '2026-02-01 09:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (24, 13, 'SIGNE', '2026-02-01 10:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (25, 5, 'CREE', '2026-01-12 08:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (26, 5, 'VALIDE', '2026-01-12 09:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (27, 5, 'SIGNE', '2026-01-12 10:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (28, 8, 'CREE', '2026-01-20 08:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (29, 8, 'VALIDE', '2026-01-20 09:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (30, 8, 'SIGNE', '2026-01-20 10:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (31, 6, 'CREE', '2026-01-15 08:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (32, 6, 'VALIDE', '2026-01-15 09:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (33, 6, 'SIGNE', '2026-01-15 10:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (34, 16, 'CREE', '2026-02-05 08:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (35, 16, 'VALIDE', '2026-02-05 09:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (36, 16, 'SIGNE', '2026-02-05 10:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (37, 4, 'CREE', '2026-01-10 08:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (38, 4, 'VALIDE', '2026-01-10 09:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (39, 4, 'SIGNE', '2026-01-10 10:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (40, 1, 'CREE', '2026-01-02 08:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (41, 1, 'VALIDE', '2026-01-02 09:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (42, 1, 'SIGNE', '2026-01-02 10:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (43, 3, 'CREE', '2026-01-05 08:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (44, 3, 'VALIDE', '2026-01-05 09:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (45, 3, 'SIGNE', '2026-01-05 10:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (46, 14, 'CREE', '2026-02-03 08:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (47, 14, 'VALIDE', '2026-02-03 09:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (48, 14, 'SIGNE', '2026-02-03 10:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (49, 9, 'CREE', '2026-01-22 08:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (50, 9, 'VALIDE', '2026-01-22 09:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (51, 9, 'SIGNE', '2026-01-22 10:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (52, 7, 'CREE', '2026-01-15 08:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (53, 7, 'VALIDE', '2026-01-15 09:00:00');
INSERT INTO etat_contrat (id, id_contrat, statut, date) VALUES (54, 7, 'SIGNE', '2026-01-15 10:00:00');

-- grille_salariale (1)
INSERT INTO grille_salariale (id, date_effectif, date_fin, statut, id_user_propose, id_user_decision, date_decision, commentaire) VALUES (1, '2026-01-01', NULL, 'VALIDEE', NULL, NULL, NULL, NULL);

-- grille_salariale_details (9)
INSERT INTO grille_salariale_details (id, id_grille, id_categorie, id_echelon, montant_min, montant_max) VALUES (1, 1, 1, 1, 3000000.00, 4000000.00);
INSERT INTO grille_salariale_details (id, id_grille, id_categorie, id_echelon, montant_min, montant_max) VALUES (2, 1, 1, 2, 4000000.00, 6000000.00);
INSERT INTO grille_salariale_details (id, id_grille, id_categorie, id_echelon, montant_min, montant_max) VALUES (3, 1, 1, 3, 6000000.00, 10000000.00);
INSERT INTO grille_salariale_details (id, id_grille, id_categorie, id_echelon, montant_min, montant_max) VALUES (4, 1, 2, 1, 1500000.00, 2000000.00);
INSERT INTO grille_salariale_details (id, id_grille, id_categorie, id_echelon, montant_min, montant_max) VALUES (5, 1, 2, 2, 2000000.00, 3000000.00);
INSERT INTO grille_salariale_details (id, id_grille, id_categorie, id_echelon, montant_min, montant_max) VALUES (6, 1, 2, 3, 3000000.00, 4000000.00);
INSERT INTO grille_salariale_details (id, id_grille, id_categorie, id_echelon, montant_min, montant_max) VALUES (7, 1, 3, 1, 800000.00, 1000000.00);
INSERT INTO grille_salariale_details (id, id_grille, id_categorie, id_echelon, montant_min, montant_max) VALUES (8, 1, 3, 2, 1000000.00, 1300000.00);
INSERT INTO grille_salariale_details (id, id_grille, id_categorie, id_echelon, montant_min, montant_max) VALUES (9, 1, 3, 3, 1300000.00, 1800000.00);

-- histo_besoin (5)
INSERT INTO histo_besoin (id, id_besoin, etat_avant, etat_apres, action, id_user, date_action, commentaire) VALUES (1, 2, NULL, 'EN_ATTENTE', 'CREATION', 3, '2026-10-23 14:11:21.271605', 'Demande soumise (reprise des données)');
INSERT INTO histo_besoin (id, id_besoin, etat_avant, etat_apres, action, id_user, date_action, commentaire) VALUES (2, 5, NULL, 'EN_ATTENTE', 'CREATION', 3, '2026-10-23 14:11:21.271605', 'Demande soumise (reprise des données)');
INSERT INTO histo_besoin (id, id_besoin, etat_avant, etat_apres, action, id_user, date_action, commentaire) VALUES (3, 4, NULL, 'EN_ATTENTE', 'CREATION', 3, '2026-10-23 14:11:21.271605', 'Demande soumise (reprise des données)');
INSERT INTO histo_besoin (id, id_besoin, etat_avant, etat_apres, action, id_user, date_action, commentaire) VALUES (4, 1, NULL, 'EN_ATTENTE', 'CREATION', 3, '2026-10-23 14:11:21.271605', 'Demande soumise (reprise des données)');
INSERT INTO histo_besoin (id, id_besoin, etat_avant, etat_apres, action, id_user, date_action, commentaire) VALUES (5, 3, NULL, 'EN_ATTENTE', 'CREATION', 3, '2026-10-23 14:11:21.271605', 'Demande soumise (reprise des données)');

-- histo_candidature (3)
INSERT INTO histo_candidature (id, id_candidature, etat_avant, etat_apres, id_user, date_changement, commentaire) VALUES (1, 2, NULL, 'DEPOSEE', NULL, '2026-10-21 00:00:00', 'Dépôt de la candidature (données de test)');
INSERT INTO histo_candidature (id, id_candidature, etat_avant, etat_apres, id_user, date_changement, commentaire) VALUES (2, 1, NULL, 'DEPOSEE', NULL, '2026-10-21 00:00:00', 'Dépôt de la candidature (données de test)');
INSERT INTO histo_candidature (id, id_candidature, etat_avant, etat_apres, id_user, date_changement, commentaire) VALUES (3, 3, NULL, 'DEPOSEE', NULL, '2026-10-21 00:00:00', 'Dépôt de la candidature (données de test)');

-- workflow_onboarding (1)
INSERT INTO workflow_onboarding (id, libelle, statut) VALUES (1, 'Intégration standard', 'ACTIF');

-- workflow_onboarding_details (7)
INSERT INTO workflow_onboarding_details (id, id_workflow, ordre, type_tache, id_poste_responsable, delai_jours, obligatoire, libelle) VALUES (1, 1, 1, 'CONTRAT', 18, 0, true, 'Classer le contrat signé dans le dossier du personnel');
INSERT INTO workflow_onboarding_details (id, id_workflow, ordre, type_tache, id_poste_responsable, delai_jours, obligatoire, libelle) VALUES (2, 1, 2, 'BADGE', 3, 1, true, 'Remettre le badge d''accès');
INSERT INTO workflow_onboarding_details (id, id_workflow, ordre, type_tache, id_poste_responsable, delai_jours, obligatoire, libelle) VALUES (3, 1, 3, 'COMPTE_SI', 4, 1, true, 'Créer le compte informatique et la messagerie');
INSERT INTO workflow_onboarding_details (id, id_workflow, ordre, type_tache, id_poste_responsable, delai_jours, obligatoire, libelle) VALUES (4, 1, 4, 'MATERIEL', 15, 2, true, 'Remettre le matériel (poste de travail, téléphone)');
INSERT INTO workflow_onboarding_details (id, id_workflow, ordre, type_tache, id_poste_responsable, delai_jours, obligatoire, libelle) VALUES (5, 1, 5, 'FORMATION', NULL, 5, true, 'Accueil au poste et formation de prise de fonction');
INSERT INTO workflow_onboarding_details (id, id_workflow, ordre, type_tache, id_poste_responsable, delai_jours, obligatoire, libelle) VALUES (6, 1, 6, 'PAIE', 17, 7, true, 'Inscrire le salarié à la paie');
INSERT INTO workflow_onboarding_details (id, id_workflow, ordre, type_tache, id_poste_responsable, delai_jours, obligatoire, libelle) VALUES (7, 1, 7, 'BANQUE', 17, 7, false, 'Enregistrer les coordonnées bancaires');

-- role (6)
INSERT INTO role (id, libelle) VALUES (1, 'Manager');
INSERT INTO role (id, libelle) VALUES (2, 'RH');
INSERT INTO role (id, libelle) VALUES (3, 'Finance');
INSERT INTO role (id, libelle) VALUES (4, 'DG');
INSERT INTO role (id, libelle) VALUES (5, 'ADMIN');
INSERT INTO role (id, libelle) VALUES (6, 'Employe');

-- workflow_candidature (4)
INSERT INTO workflow_candidature (id, ordre, type_evaluation, score_min, id_offre_emploi, responsable_regle, id_poste_responsable) VALUES (1, 1, 'TEST', 10.00, NULL, 'SUPERIEUR', NULL);
INSERT INTO workflow_candidature (id, ordre, type_evaluation, score_min, id_offre_emploi, responsable_regle, id_poste_responsable) VALUES (3, 3, 'ENTRETIEN_CHEF_DEPARTEMENT', 12.00, NULL, 'RESPONSABLE_UNITE', NULL);
INSERT INTO workflow_candidature (id, ordre, type_evaluation, score_min, id_offre_emploi, responsable_regle, id_poste_responsable) VALUES (2, 2, 'ENTRETIEN_RH', 12.00, NULL, 'POSTE_FIXE', 18);
INSERT INTO workflow_candidature (id, ordre, type_evaluation, score_min, id_offre_emploi, responsable_regle, id_poste_responsable) VALUES (4, 4, 'ENTRETIEN_DIRECTION', NULL, NULL, 'POSTE_FIXE', 1);

-- user_role (20)
INSERT INTO user_role (id, id_user, id_role) VALUES (1, 1, 1);
INSERT INTO user_role (id, id_user, id_role) VALUES (2, 2, 1);
INSERT INTO user_role (id, id_user, id_role) VALUES (3, 3, 2);
INSERT INTO user_role (id, id_user, id_role) VALUES (4, 4, 3);
INSERT INTO user_role (id, id_user, id_role) VALUES (5, 5, 4);
INSERT INTO user_role (id, id_user, id_role) VALUES (6, 6, 5);
INSERT INTO user_role (id, id_user, id_role) VALUES (7, 8, 6);
INSERT INTO user_role (id, id_user, id_role) VALUES (8, 9, 6);
INSERT INTO user_role (id, id_user, id_role) VALUES (9, 17, 1);
INSERT INTO user_role (id, id_user, id_role) VALUES (10, 16, 1);
INSERT INTO user_role (id, id_user, id_role) VALUES (11, 15, 1);
INSERT INTO user_role (id, id_user, id_role) VALUES (12, 13, 1);
INSERT INTO user_role (id, id_user, id_role) VALUES (13, 11, 1);
INSERT INTO user_role (id, id_user, id_role) VALUES (14, 17, 2);
INSERT INTO user_role (id, id_user, id_role) VALUES (15, 20, 6);
INSERT INTO user_role (id, id_user, id_role) VALUES (16, 19, 6);
INSERT INTO user_role (id, id_user, id_role) VALUES (17, 18, 6);
INSERT INTO user_role (id, id_user, id_role) VALUES (18, 14, 6);
INSERT INTO user_role (id, id_user, id_role) VALUES (19, 12, 6);
INSERT INTO user_role (id, id_user, id_role) VALUES (20, 10, 6);

-- workflow_validation (4)
INSERT INTO workflow_validation (id, id_poste, ordre, controle) VALUES (1, 3, 1, NULL);
INSERT INTO workflow_validation (id, id_poste, ordre, controle) VALUES (2, 18, 1, NULL);
INSERT INTO workflow_validation (id, id_poste, ordre, controle) VALUES (4, 1, 3, NULL);
INSERT INTO workflow_validation (id, id_poste, ordre, controle) VALUES (3, 2, 2, 'BUDGET');

-- Compteurs des id : les prochains enregistrements continuent après les données
SELECT setval('affectation_id_seq', 18, true);
SELECT setval('besoin_id_seq', 5, true);
SELECT setval('besoin_poste_id_seq', 6, true);
SELECT setval('budget_unite_id_seq', 10, true);
SELECT setval('candidat_detail_id_seq', 1, false);
SELECT setval('candidat_id_seq', 3, true);
SELECT setval('candidatures_id_seq', 3, true);
SELECT setval('categorie_metier_id_seq', 3, true);
SELECT setval('contrat_details_id_seq', 1, false);
SELECT setval('contrat_id_seq', 18, true);
SELECT setval('contrat_remuneration_id_seq', 18, true);
SELECT setval('coordonnees_bancaires_id_seq', 1, false);
SELECT setval('delegation_id_seq', 1, false);
SELECT setval('demande_contrat_id_seq', 1, false);
SELECT setval('document_candidat_id_seq', 1, false);
SELECT setval('document_id_seq', 1, false);
SELECT setval('echelon_metier_id_seq', 3, true);
SELECT setval('employe_id_seq', 18, true);
SELECT setval('etat_contrat_id_seq', 54, true);
SELECT setval('fiche_poste_id_seq', 10, true);
SELECT setval('grille_salariale_details_id_seq', 9, true);
SELECT setval('grille_salariale_id_seq', 1, true);
SELECT setval('histo_besoin_id_seq', 5, true);
SELECT setval('histo_candidature_id_seq', 3, true);
SELECT setval('histo_proposition_embauche_id_seq', 1, false);
SELECT setval('journal_audit_id_seq', 1, false);
SELECT setval('metier_id_seq', 10, true);
SELECT setval('motif_id_seq', 1, true);
SELECT setval('notification_id_seq', 1, false);
SELECT setval('offre_emploi_id_seq', 6, true);
SELECT setval('onboarding_details_id_seq', 1, false);
SELECT setval('onboarding_id_seq', 1, false);
SELECT setval('personne_id_seq', 3, true);
SELECT setval('point_condition_id_seq', 6, true);
SELECT setval('poste_id_seq', 18, true);
SELECT setval('proposition_condition_id_seq', 1, false);
SELECT setval('proposition_embauche_id_seq', 1, false);
SELECT setval('relance_condition_id_seq', 1, false);
SELECT setval('role_id_seq', 6, true);
SELECT setval('traitement_detail_id_seq', 1, false);
SELECT setval('traitement_id_seq', 1, false);
SELECT setval('type_contrat_id_seq', 3, true);
SELECT setval('type_document_id_seq', 12, true);
SELECT setval('type_information_id_seq', 3, true);
SELECT setval('unite_orga_id_seq', 10, true);
SELECT setval('user_id_seq', 20, true);
SELECT setval('user_role_id_seq', 20, true);
SELECT setval('validation_id_seq', 1, false);
SELECT setval('workflow_candidature_id_seq', 4, true);
SELECT setval('workflow_onboarding_details_id_seq', 7, true);
SELECT setval('workflow_onboarding_id_seq', 1, true);
SELECT setval('workflow_validation_id_seq', 4, true);

COMMIT;
