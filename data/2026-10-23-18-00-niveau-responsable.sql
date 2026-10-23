-- =====================================================================
-- Poste responsable d'un niveau de sélection : règle automatique.
--   SUPERIEUR         : supérieur direct du poste à pourvoir de l'offre
--   RESPONSABLE_UNITE : poste responsable de l'unité du poste à pourvoir (ou d'une unité parente)
--   POSTE_FIXE        : un poste choisi pour ce niveau (id_poste_responsable)
--   NULL              : choisi par la RH à chaque planification
-- Peut être relancé sans erreur.
-- =====================================================================
ALTER TABLE workflow_candidature ADD COLUMN IF NOT EXISTS responsable_regle VARCHAR(30) NULL;
ALTER TABLE workflow_candidature ADD COLUMN IF NOT EXISTS id_poste_responsable INT NULL REFERENCES poste(id);
ALTER TABLE workflow_candidature DROP CONSTRAINT IF EXISTS ck_workflow_candidature_regle;
ALTER TABLE workflow_candidature ADD CONSTRAINT ck_workflow_candidature_regle
    CHECK (responsable_regle IS NULL OR responsable_regle IN ('SUPERIEUR', 'RESPONSABLE_UNITE', 'POSTE_FIXE'));

-- Réglage de départ des niveaux communs (entretien RH : chargé de recrutement, POST-018) (modifiable dans l'écran Niveaux de sélection)
UPDATE workflow_candidature SET responsable_regle = 'SUPERIEUR'
 WHERE id_offre_emploi IS NULL AND responsable_regle IS NULL AND type_evaluation = 'TEST';
UPDATE workflow_candidature SET responsable_regle = 'RESPONSABLE_UNITE'
 WHERE id_offre_emploi IS NULL AND responsable_regle IS NULL AND type_evaluation = 'ENTRETIEN_CHEF_DEPARTEMENT';
UPDATE workflow_candidature w SET responsable_regle = 'POSTE_FIXE',
       id_poste_responsable = (SELECT id FROM poste WHERE code = 'POST-018' AND etat = 'ACTIF')
 WHERE w.id_offre_emploi IS NULL AND w.responsable_regle IS NULL AND w.type_evaluation = 'ENTRETIEN_RH'
   AND EXISTS (SELECT 1 FROM poste WHERE code = 'POST-018' AND etat = 'ACTIF');
UPDATE workflow_candidature w SET responsable_regle = 'POSTE_FIXE',
       id_poste_responsable = (SELECT id FROM poste WHERE libelle = 'Directeur general' AND etat = 'ACTIF' ORDER BY id LIMIT 1)
 WHERE w.id_offre_emploi IS NULL AND w.responsable_regle IS NULL AND w.type_evaluation = 'ENTRETIEN_DIRECTION'
   AND EXISTS (SELECT 1 FROM poste WHERE libelle = 'Directeur general' AND etat = 'ACTIF');
