-- V1__init_schema.sql
-- MySQL 8.x
-- Schema matches current JPA entities:
-- users, jobs, applications, application_tasks

-- ---------- users ----------
CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       created_at DATETIME(6) NOT NULL,
                       updated_at DATETIME(6) NOT NULL,

                       email VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(64) NOT NULL,

                       PRIMARY KEY (id),
                       UNIQUE KEY uk_users_email (email),
                       KEY idx_users_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- jobs ----------
CREATE TABLE jobs (
                      id BIGINT NOT NULL AUTO_INCREMENT,
                      created_at DATETIME(6) NOT NULL,
                      updated_at DATETIME(6) NOT NULL,

                      title VARCHAR(255) NOT NULL,
                      company VARCHAR(255) NOT NULL,
                      location VARCHAR(255) NULL,

                      PRIMARY KEY (id),
                      KEY idx_jobs_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- applications ----------
-- Note:
-- 1) requestId in entity -> column name defaults to request_id under Spring/Hibernate naming strategy.
--    To be explicit and stable, we create request_id here.
-- 2) user/job relations: JPA will create user_id and job_id columns.
-- 3) Application has @Version Long version, retryCount int, status enum string
CREATE TABLE applications (
                              id BIGINT NOT NULL AUTO_INCREMENT,
                              created_at DATETIME(6) NOT NULL,
                              updated_at DATETIME(6) NOT NULL,

                              request_id VARCHAR(255) NOT NULL,
                              user_id BIGINT NOT NULL,
                              job_id BIGINT NOT NULL,

                              status VARCHAR(16) NOT NULL,
                              retry_count INT NOT NULL,
                              version BIGINT NOT NULL,

                              PRIMARY KEY (id),

                              CONSTRAINT uk_app_request_id UNIQUE (request_id),

                              KEY idx_app_status (status),
                              KEY idx_app_created_at (created_at),
                              KEY idx_app_user_created_at (user_id, created_at),
                              KEY idx_app_job_created_at (job_id, created_at),

                              CONSTRAINT fk_app_user FOREIGN KEY (user_id) REFERENCES users(id),
                              CONSTRAINT fk_app_job  FOREIGN KEY (job_id)  REFERENCES jobs(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- application_tasks ----------
-- Note:
-- application_id is a plain FK-like column but not mapped as @ManyToOne in entity.
-- Still, we can enforce referential integrity by adding FK to applications(id).
CREATE TABLE application_tasks (
                                   id BIGINT NOT NULL AUTO_INCREMENT,

                                   application_id BIGINT NOT NULL,

                                   status VARCHAR(16) NOT NULL,
                                   retry_count INT NOT NULL,
                                   max_retries INT NOT NULL,
                                   next_run_at DATETIME(6) NOT NULL,

                                   locked_by VARCHAR(64) NULL,
                                   locked_at DATETIME(6) NULL,

                                   last_error VARCHAR(1000) NULL,

                                   created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                   updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                                   version BIGINT NOT NULL,

                                   PRIMARY KEY (id),

                                   CONSTRAINT uk_application_tasks_application_id UNIQUE (application_id),
                                   KEY idx_tasks_runnable (status, next_run_at),

                                   CONSTRAINT fk_tasks_application FOREIGN KEY (application_id) REFERENCES applications(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;