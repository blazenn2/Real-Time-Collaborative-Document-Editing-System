--liquibase formatted sql
--changeset Hamza:1.0.6-add-UUID-table-for-storing-consumed-uuids.sql endDelimiter:#

CREATE TABLE uuid_history (
    document_id BIGINT NOT NULL,
    uuid UUID NOT NULL PRIMARY KEY,
    CONSTRAINT fk_uuid_history_document FOREIGN KEY (document_id)
        REFERENCES document(id)
        ON DELETE CASCADE
);#
