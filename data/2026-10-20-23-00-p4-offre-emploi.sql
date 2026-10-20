-- P4 (Fifaliana) : Gestion des offres d'emploi (côté RH)


-- Le type de contrat est facultatif sur l'offre (id_type_contrat peut être vide)
ALTER TABLE offre_emploi ALTER COLUMN id_type_contrat DROP NOT NULL;
