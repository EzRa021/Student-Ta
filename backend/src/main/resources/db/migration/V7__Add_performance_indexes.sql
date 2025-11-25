-- V7__Add_performance_indexes.sql
-- ✅ PERFORMANCE FIX: Create strategic indexes on frequently queried columns

-- User table indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_student_id ON users(student_id);
CREATE INDEX idx_users_created_at ON users(created_at);

-- Request table indexes
CREATE INDEX idx_requests_status ON requests(status);
CREATE INDEX idx_requests_student_id ON requests(student_id);
CREATE INDEX idx_requests_assigned_to ON requests(assigned_to);
CREATE INDEX idx_requests_created_at ON requests(created_at);
CREATE INDEX idx_requests_status_created ON requests(status, created_at);

-- Reply table indexes
CREATE INDEX idx_replies_request_id ON replies(request_id);
CREATE INDEX idx_replies_created_at ON replies(created_at);

-- Audit logs indexes
CREATE INDEX idx_audit_logs_performed_by ON audit_logs(performed_by);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);

-- Lab session indexes
CREATE INDEX idx_lab_sessions_created_at ON lab_sessions(created_at);

