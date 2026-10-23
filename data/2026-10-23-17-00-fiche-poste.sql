-- =====================================================================
-- Fiches de poste : un modèle standard par métier, réutilisé par tous les postes de ce métier,
-- avec une fiche propre à un poste si besoin (elle prime sur celle du métier).
-- Rattachement aux offres : l'offre garde la fiche utilisée (id_fiche_poste).
-- Peut être relancé sans erreur.
-- =====================================================================
CREATE TABLE IF NOT EXISTS fiche_poste (
    id SERIAL PRIMARY KEY,
    id_metier INT NULL REFERENCES metier(id),
    id_poste INT NULL REFERENCES poste(id),
    mission TEXT NOT NULL,
    activites TEXT NULL,
    formation TEXT NULL,
    experience TEXT NULL,
    competences TEXT NULL,
    conditions TEXT NULL,
    version INT NOT NULL DEFAULT 1,
    date_maj TIMESTAMP NOT NULL DEFAULT now(),
    id_auteur INT NULL REFERENCES "user"(id),
    CONSTRAINT ck_fiche_poste_portee CHECK ((id_metier IS NOT NULL) <> (id_poste IS NOT NULL))
);
CREATE UNIQUE INDEX IF NOT EXISTS ux_fiche_poste_metier ON fiche_poste(id_metier) WHERE id_metier IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_fiche_poste_poste ON fiche_poste(id_poste) WHERE id_poste IS NOT NULL;

ALTER TABLE offre_emploi ADD COLUMN IF NOT EXISTS id_fiche_poste INT NULL REFERENCES fiche_poste(id);

-- Modèle standard de départ pour chaque métier existant (à compléter par la RH)
INSERT INTO fiche_poste (id_metier, mission, activites, formation, experience, competences, conditions)
SELECT m.id,
       'Contribuer, en tant que ' || m.libelle || ', aux objectifs de son unité et de l''entreprise, dans le respect des procédures et des délais fixés.',
       E'Réaliser les tâches propres au métier de ' || m.libelle || E'.\nRendre compte régulièrement de son activité à son supérieur direct.\nRespecter les procédures internes, la confidentialité et les règles de sécurité.\nCollaborer avec les autres équipes et partager l''information utile.',
       'Diplôme ou qualification en rapport avec le métier de ' || m.libelle || '.',
       'Expérience adaptée au niveau du poste (voir l''échelon).',
       E'Maîtrise des techniques du métier.\nSens de l''organisation et de la rigueur.\nCapacité de travail en équipe et bonne communication.',
       E'Temps plein, 40 heures par semaine.\nLieu de travail : siège de l''entreprise.'
FROM metier m
WHERE NOT EXISTS (SELECT 1 FROM fiche_poste f WHERE f.id_metier = m.id);
