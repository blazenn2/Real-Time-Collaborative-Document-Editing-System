--liquibase formatted sql
--changeset Hamza:1.0.2-creation-of-dead-events-entity.sql endDelimiter:#

CREATE TABLE dead_events (
    id BIGINT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    payload jsonb NOT NULL,
    content_body TEXT,
    event_Type VARCHAR(255),
    headers jsonb NOT NULL,
    user_id BIGINT,
    routing_key VARCHAR(255),
    created_date TIMESTAMP
);#

CREATE SEQUENCE dead_events_seq
    INCREMENT BY 1
    START WITH 1
    OWNED BY dead_events.id;#