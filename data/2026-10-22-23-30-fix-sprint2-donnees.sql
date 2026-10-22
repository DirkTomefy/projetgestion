-- =====================================================================
-- Correctifs sprint 2 (branche fix-dev-nathan) : données de référence manquantes.
-- Peut être relancé sans erreur.
-- =====================================================================

-- Copie signée d'un contrat, déposée par la RH avant la signature (ContratService.deposerSigne)
INSERT INTO type_document (libelle)
SELECT 'CONTRAT_SIGNE'
WHERE NOT EXISTS (SELECT 1 FROM type_document WHERE libelle = 'CONTRAT_SIGNE');
