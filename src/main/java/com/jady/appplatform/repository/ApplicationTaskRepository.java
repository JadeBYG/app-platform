package com.jady.appplatform.repository;

import com.jady.appplatform.domain.entity.ApplicationTask;
import com.jady.appplatform.domain.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ApplicationTaskRepository extends JpaRepository<ApplicationTask, Long> {

    // 1) 并发安全：在事务里锁定一批可执行任务（跳过已被其他事务锁住的行）
    @Query(value = """
        SELECT *
        FROM application_tasks
        WHERE status = 'PENDING'
          AND next_run_at <= NOW()
        ORDER BY next_run_at ASC
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<ApplicationTask> lockRunnableTasks(int limit);

    // 2) 把任务标记为 RUNNING（批量更新）
    @Modifying
    @Query(value = """
        UPDATE application_tasks
        SET status = 'RUNNING',
            locked_by = :workerId,
            locked_at = NOW(),
            last_error = NULL
        WHERE id IN (:ids)
        """, nativeQuery = true)
    int markRunningBatch(
            @org.springframework.data.repository.query.Param("ids") List<Long> ids,
            @org.springframework.data.repository.query.Param("workerId") String workerId
    );

    @Modifying
    @Query(value = """
    UPDATE application_tasks
    SET status = 'PENDING',
        locked_by = NULL,
        locked_at = NULL,
        next_run_at = NOW()
    WHERE status = 'RUNNING'
      AND locked_at < DATE_SUB(NOW(), INTERVAL :timeoutSeconds SECOND)
    """, nativeQuery = true)
    int releaseStuckTasks(int timeoutSeconds);

    @org.springframework.data.jpa.repository.Query(value = """
    SELECT status, COUNT(*) as cnt
    FROM application_tasks
    GROUP BY status
    """, nativeQuery = true)
    java.util.List<Object[]> countByStatusNative();

    Optional<ApplicationTask> findByApplicationId(Long applicationId);

    List<ApplicationTask> findTop50ByStatusAndNextRunAtLessThanEqualOrderByNextRunAtAsc(
            TaskStatus status,
            LocalDateTime now
    );
}
