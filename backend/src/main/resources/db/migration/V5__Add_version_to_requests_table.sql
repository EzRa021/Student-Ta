-- V5__Add_version_to_requests_table.sql
-- Add version column for optimistic locking in the requests table

ALTER TABLE requests ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
