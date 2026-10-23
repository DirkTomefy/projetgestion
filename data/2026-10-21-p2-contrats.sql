ALTER TABLE contrat ADD COLUMN IF NOT EXISTS motif_fin VARCHAR(255) NULL;

ALTER TABLE contrat DROP CONSTRAINT IF EXISTS contrat_statut_actuel_check;
ALTER TABLE contrat ADD CONSTRAINT contrat_statut_actuel_check
    CHECK (statut_actuel IN ('CREE', 'VALIDE', 'SIGNE', 'TERMINE', 'ANNULE'));

ALTER TABLE etat_contrat DROP CONSTRAINT IF EXISTS etat_contrat_statut_check;
ALTER TABLE etat_contrat ADD CONSTRAINT etat_contrat_statut_check
    CHECK (statut IN ('CREE', 'VALIDE', 'SIGNE', 'TERMINE', 'ANNULE'));

ALTER TABLE besoin_poste ADD COLUMN IF NOT EXISTS id_employe_remplace INT NULL;
ALTER TABLE besoin_poste DROP CONSTRAINT IF EXISTS fk_besoin_poste_employe_remplace;
ALTER TABLE besoin_poste ADD CONSTRAINT fk_besoin_poste_employe_remplace
    FOREIGN KEY (id_employe_remplace) REFERENCES employe(id);

INSERT INTO type_contrat (libelle, duree_max, renouvellement_max)
SELECT 'CE', 6, 2 WHERE NOT EXISTS (SELECT 1 FROM type_contrat WHERE libelle = 'CE');
INSERT INTO type_contrat (libelle, duree_max, renouvellement_max)
SELECT 'CDD', 24, 1 WHERE NOT EXISTS (SELECT 1 FROM type_contrat WHERE libelle = 'CDD');
INSERT INTO type_contrat (libelle, duree_max, renouvellement_max)
SELECT 'CDI', NULL, NULL WHERE NOT EXISTS (SELECT 1 FROM type_contrat WHERE libelle = 'CDI');

UPDATE type_contrat SET duree_max = 6,  renouvellement_max = 2 WHERE libelle = 'CE';
UPDATE type_contrat SET duree_max = 24, renouvellement_max = 1 WHERE libelle = 'CDD';