-- V5__jobs_owner_status.sql

ALTER TABLE jobs
    ADD COLUMN description TEXT NOT NULL,
    ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
    ADD COLUMN owner_id BIGINT NOT NULL;

-- 给 owner_id 建索引，后面按雇主查职位会用到
CREATE INDEX idx_jobs_owner_id ON jobs(owner_id);

-- status 列也会被过滤/列表用
CREATE INDEX idx_jobs_status ON jobs(status);

-- 外键
ALTER TABLE jobs
    ADD CONSTRAINT fk_jobs_owner
        FOREIGN KEY (owner_id) REFERENCES users(id);
