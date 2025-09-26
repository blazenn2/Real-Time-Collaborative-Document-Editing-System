--liquibase formatted sql
--changeset Hamza:1.0.5-add-UUID-column-in-document-table.sql endDelimiter:#

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";#

ALTER TABLE document ADD COLUMN uuid UUID;#

UPDATE document SET uuid = uuid_generate_v4() WHERE uuid IS NULL;#

ALTER TABLE document ALTER COLUMN uuid SET NOT NULL;#

ALTER TABLE document ADD CONSTRAINT uq_document_uuid UNIQUE (uuid);#

