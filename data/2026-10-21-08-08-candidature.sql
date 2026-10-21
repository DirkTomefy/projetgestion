INSERT INTO
    type_information (libelle)
VALUES
    ('EXPERIENCE PRO'),
    ('EDUCATION'),
    ('QUALITE') ON CONFLICT (libelle) DO NOTHING;

CREATE UNIQUE INDEX IF NOT EXISTS ux_candidat_email ON candidat (lower(email));

CREATE UNIQUE INDEX IF NOT EXISTS uq_document_candidat ON document_candidat (id_candidature, id_document);

ALTER TABLE candidat_detail
DROP CONSTRAINT IF EXISTS ck_candidat_detail_dates;

ALTER TABLE candidat_detail ADD CONSTRAINT ck_candidat_detail_dates CHECK (
    date_fin IS NULL
    OR date_debut IS NULL
    OR date_fin >= date_debut
);