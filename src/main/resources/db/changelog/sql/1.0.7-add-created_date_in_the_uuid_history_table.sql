--liquibase formatted sql
--changeset Hamza:1.0.7-add-created_date_in_the_uuid_history_table.sql endDelimiter:#

ALTER TABLE uuid_history ADD COLUMN created_date TIMESTAMP;#