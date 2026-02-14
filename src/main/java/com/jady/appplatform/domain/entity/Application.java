package com.jady.appplatform.domain.entity;

import com.jady.appplatform.domain.enums.ApplicationStatus;
import jakarta.persistence.*;

@Entity
@Table(
        name = "applications",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_app_request_id", columnNames = "request_id")
        },
        indexes = {
                @Index(name = "idx_app_status", columnList = "status"),
                @Index(name = "idx_app_created_at", columnList = "created_at"),
                @Index(name = "idx_app_user_created_at", columnList = "user_id, created_at"),
                @Index(name = "idx_app_job_created_at", columnList = "job_id, created_at")
        }
)
public class Application extends BaseEntity {

    @Column(name = "request_id", nullable = false, updatable = false)
    private String requestId; // 幂等关键字段

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(nullable = false)
    private int retryCount;

    @Version
    @Column(nullable = false)
    private Long version;

    protected Application() {
    }

    public Application(String requestId, User user, Job job) {
        this.requestId = requestId;
        this.user = user;
        this.job = job;
        this.status = ApplicationStatus.PENDING;
        this.retryCount = 0;
    }
}
