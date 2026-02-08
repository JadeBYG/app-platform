-- V2__constraints_and_indexes.sql

-- jobs: 常用检索索引（后面列表页/搜索会用）
CREATE INDEX idx_jobs_company ON jobs(company);
CREATE INDEX idx_jobs_location ON jobs(location);
CREATE INDEX idx_jobs_title ON jobs(title);

-- applications: retry_count 默认值（避免未来 insert 时漏字段）
ALTER TABLE applications
    ALTER COLUMN retry_count SET DEFAULT 0;

-- users: 角色字段长度固定后可加索引（可选）
CREATE INDEX idx_users_role ON users(role);