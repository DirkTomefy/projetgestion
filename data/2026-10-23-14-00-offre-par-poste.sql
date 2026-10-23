-- =====================================================================
-- Une offre d'emploi par poste : un besoin qui demande plusieurs postes différents
-- (ex. comptable senior et comptable junior) donne une offre distincte pour chacun.
-- Les offres existantes sur un besoin à plusieurs postes sont séparées : l'offre garde le premier poste,
-- une offre « réf-2 », « réf-3 »… est créée pour chacun des autres (sans PDF, à régénérer si besoin).
-- Peut être relancé sans erreur.
-- =====================================================================
ALTER TABLE offre_emploi ADD COLUMN IF NOT EXISTS id_poste INT NULL REFERENCES poste(id);

-- Offres existantes dont le besoin ne porte que sur un poste : rattachées à ce poste
UPDATE offre_emploi o SET id_poste = x.id_poste
FROM (SELECT id_besoin, MIN(id_poste) AS id_poste FROM besoin_poste
      GROUP BY id_besoin HAVING COUNT(DISTINCT id_poste) = 1) x
WHERE x.id_besoin = o.id_besoin AND o.id_poste IS NULL;

-- Offres existantes dont le besoin porte sur plusieurs postes : une offre par poste
CREATE TEMP TABLE offre_postes AS
SELECT o.id AS id_offre, x.id_poste, ROW_NUMBER() OVER (PARTITION BY o.id ORDER BY x.ordre) AS rang
FROM offre_emploi o
JOIN (SELECT id_besoin, id_poste, MIN(id) AS ordre FROM besoin_poste GROUP BY id_besoin, id_poste) x ON x.id_besoin = o.id_besoin
WHERE o.id_poste IS NULL;

INSERT INTO offre_emploi (id_besoin, id_poste, ref, id_type_contrat, date_publication, date_limite_candidature,
                          id_document, etat, date_cloture, motif_cloture, id_user_cloture)
SELECT o.id_besoin, op.id_poste, o.ref || '-' || op.rang, o.id_type_contrat, o.date_publication, o.date_limite_candidature,
       NULL, o.etat, o.date_cloture, o.motif_cloture, o.id_user_cloture
FROM offre_postes op JOIN offre_emploi o ON o.id = op.id_offre
WHERE op.rang > 1 AND NOT EXISTS (SELECT 1 FROM offre_emploi x WHERE x.ref = o.ref || '-' || op.rang);

UPDATE offre_emploi o SET id_poste = op.id_poste
FROM offre_postes op WHERE op.id_offre = o.id AND op.rang = 1;

DROP TABLE offre_postes;

CREATE INDEX IF NOT EXISTS idx_offre_emploi_besoin_poste ON offre_emploi (id_besoin, id_poste);
