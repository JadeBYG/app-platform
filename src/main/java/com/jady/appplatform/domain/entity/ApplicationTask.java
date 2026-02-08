package com.jady.appplatform.domain.entity;

import com.jady.appplatform.domain.enums.TaskStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "application_tasks",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_application_tasks_application_id", columnNames = "application_id")
        },
        indexes = {
                @Index(name = "idx_tasks_runnable", columnList = "status,next_run_at")
        }
)
public class ApplicationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 只存 application_id，不强绑定到 Application 实体，避免不必要的懒加载/序列化问题
    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TaskStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "max_retries", nullable = false)
    private int maxRetries;

    @Column(name = "next_run_at", nullable = false)
    private LocalDateTime nextRunAt;

    @Column(name = "locked_by", length = 64)
    private String lockedBy;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected ApplicationTask() {
        // for JPA
    }

    public ApplicationTask(Long applicationId, int maxRetries, LocalDateTime nextRunAt) {
        this.applicationId = applicationId;
        this.status = TaskStatus.PENDING;
        this.retryCount = 0;
        this.maxRetries = maxRetries;
        this.nextRunAt = nextRunAt;
    }

    // ====== getters ======
    public Long getId() { return id; }
    public Long getApplicationId() { return applicationId; }
    public TaskStatus getStatus() { return status; }
    public int getRetryCount() { return retryCount; }
    public int getMaxRetries() { return maxRetries; }
    public LocalDateTime getNextRunAt() { return nextRunAt; }
    public String getLockedBy() { return lockedBy; }
    public LocalDateTime getLockedAt() { return lockedAt; }
    public String getLastError() { return lastError; }

    // ====== state transitions ======
    public void markRunning(String workerId, LocalDateTime now) {
        this.status = TaskStatus.RUNNING;
        this.lockedBy = workerId;
        this.lockedAt = now;
        this.lastError = null;
    }

    public void markSuccess() {
        this.status = TaskStatus.SUCCESS;
        this.lastError = null;
    }

    public void markFailureAndScheduleRetry(String error, LocalDateTime nextRunAt) {
        this.retryCount += 1;
        this.lastError = truncate(error, 1000);

        if (this.retryCount >= this.maxRetries) {
            this.status = TaskStatus.FAILED;
        } else {
            this.status = TaskStatus.PENDING;
            this.nextRunAt = nextRunAt;
        }
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }
}
