-- V4__add_user_role.sql
-- Ensure users.role is one of: USER / EMPLOYER / ADMIN

-- 1) 统一大小写，避免出现 user / Employer 等导致鉴权拼接 ROLE_... 混乱
UPDATE users
SET role = UPPER(role)
WHERE role IS NOT NULL;

-- 2) 把 NULL/空串兜底成 USER（理论上 V1 已 NOT NULL，但以防历史手动改过）
UPDATE users
SET role = 'USER'
WHERE role IS NULL OR role = '';

-- 3) 把非白名单值收敛成 USER（避免历史写入了其它字符串）
UPDATE users
SET role = 'USER'
WHERE role NOT IN ('USER', 'EMPLOYER', 'ADMIN');

-- 4) 可选：加 CHECK 约束（MySQL 8.0.16+ 才会 enforce；RDS MySQL 8 一般没问题）
ALTER TABLE users
    ADD CONSTRAINT chk_users_role
        CHECK (role IN ('USER','EMPLOYER','ADMIN'));