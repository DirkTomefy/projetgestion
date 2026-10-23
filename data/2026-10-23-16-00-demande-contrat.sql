-- =====================================================================
-- Demandes de nouveau contrat / avenant pour un employé en poste.
-- Origine : la RH (proposition), le manager ou l'employé (demande, à instruire par la RH).
-- Circuit : RH → (Finance si le surcoût dépasse le disponible du budget) → DG.
-- À la validation du DG : renouvellement / transformation = nouveau contrat ; augmentation = nouvelle ligne de salaire.
-- Peut être relancé sans erreur.
-- =====================================================================
CREATE TABLE IF NOT EXISTS demande_contrat (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(30) NOT NULL UNIQUE,
    id_contrat INT NOT NULL REFERENCES contrat(id),
    origine VARCHAR(10) NOT NULL,
    motif_type VARCHAR(20) NOT NULL,
    id_type_contrat INT NULL REFERENCES type_contrat(id),
    date_effet DATE NOT NULL,
    date_fin_prevue DATE NULL,
    salaire NUMERIC(15,2) NOT NULL,
    motif TEXT NULL,
    etat VARCHAR(20) NOT NULL,
    depassement_budget BOOLEAN NOT NULL DEFAULT FALSE,
    id_demandeur INT NOT NULL REFERENCES "user"(id),
    date_demande TIMESTAMP NOT NULL DEFAULT now(),
    id_rh INT NULL REFERENCES "user"(id),
    date_rh TIMESTAMP NULL,
    commentaire_rh TEXT NULL,
    id_finance INT NULL REFERENCES "user"(id),
    date_finance TIMESTAMP NULL,
    commentaire_finance TEXT NULL,
    id_dg INT NULL REFERENCES "user"(id),
    date_dg TIMESTAMP NULL,
    commentaire_dg TEXT NULL,
    id_contrat_resultat INT NULL REFERENCES contrat(id)
);
CREATE INDEX IF NOT EXISTS idx_demande_contrat_etat ON demande_contrat(etat);
CREATE INDEX IF NOT EXISTS idx_demande_contrat_contrat ON demande_contrat(id_contrat);
