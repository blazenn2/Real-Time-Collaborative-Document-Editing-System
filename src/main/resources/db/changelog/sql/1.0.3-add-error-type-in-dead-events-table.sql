--liquibase formatted sql
--changeset Hamza:1.0.3-add-error-type-in-dead-events-table.sql endDelimiter:#

DROP TABLE dead_events;#

CREATE TABLE dead_events (
    id BIGINT PRIMARY KEY,
    document_id BIGINT,
    error_type VARCHAR(255),
    payload jsonb,
    content_body TEXT,
    event_Type VARCHAR(255),
    headers jsonb,
    user_id BIGINT,
    routing_key VARCHAR(255),
    created_date TIMESTAMP
);#


CREATE SEQUENCE dead_events_seq
    INCREMENT BY 1
    START WITH 1
    OWNED BY dead_events.id;#