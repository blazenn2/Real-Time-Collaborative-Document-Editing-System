--liquibase formatted sql
--changeset Hamza:1.0.4-add-outbox-entry-table-for-cache-update.sql endDelimiter:#

CREATE TABLE outbox_entry (
    id BIGINT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    operation VARCHAR(255),
    payload jsonb,
    is_processed BOOLEAN default false,
    created_date TIMESTAMP
);#


CREATE SEQUENCE outbox_entry_seq
    INCREMENT BY 1
    START WITH 1
    OWNED BY outbox_entry.id;#