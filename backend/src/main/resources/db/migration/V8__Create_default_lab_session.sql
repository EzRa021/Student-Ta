-- V8__Create_default_lab_session.sql
-- Insert a default lab session for requests that don't specify a specific session

INSERT INTO lab_sessions (id, name, start_time, end_time, created_at, updated_at) 
VALUES (
    'DEFAULT',
    'Default Lab Session',
    DATE_SUB(NOW(), INTERVAL 30 DAY),
    DATE_ADD(NOW(), INTERVAL 30 DAY),
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE updated_at = NOW();
