package com.jady.appplatform.service;

import com.jady.appplatform.domain.entity.Application;
import com.jady.appplatform.domain.entity.ApplicationTask;
import com.jady.appplatform.domain.enums.TaskStatus;
import com.jady.appplatform.repository.ApplicationRepository;
import com.jady.appplatform.repository.ApplicationTaskRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ApplicationTaskService {

    private final ApplicationTaskRepository taskRepository;
    private final ApplicationRepository applicationRepository;
    private final EntityManager entityManager;

    public ApplicationTaskService(ApplicationTaskRepository taskRepository,
                                  ApplicationRepository applicationRepository,
                                  EntityManager entityManager) {
        this.taskRepository = taskRepository;
        this.applicationRepository = applicationRepository;
        this.entityManager = entityManager;
    }

    /**
     *      * 幂等创建：一个 applicationId 只会有一个 task（依赖 DB unique）
     *      * 并发兜底：撞 unique 就 clear + 再查一次
     */
    @Transactional
    public ApplicationTask createTaskIfAbsent(Long applicationId) {
        return taskRepository.findByApplicationId(applicationId)
                .orElseGet(() -> {
                    try {
                        ApplicationTask task = new ApplicationTask(applicationId, 5, LocalDateTime.now());
                        // 2) saveAndFlush: let throw unique conflict
                        return taskRepository.saveAndFlush(task);
                    } catch (DataIntegrityViolationException ex) {
                        // 3) clear persistence context, avoid dirty session
                        entityManager.clear();

                        // 4) check insert
                        return taskRepository.findByApplicationId(applicationId)
                                .orElseThrow(() -> ex);
                    }

                });
    }

    @Transactional
    public void markSuccess(Long taskId) {
        ApplicationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + taskId));
        task.markSuccess();

        Application app = applicationRepository.findById(task.getApplicationId())
                .orElseThrow(() -> new EntityNotFoundException("Application not found: " + task.getApplicationId()));
        app.markSuccess();
    }

    @Transactional
    public void markFailureAndScheduleRetry(Long taskId, String error) {
        ApplicationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + taskId));
        LocalDateTime nextRunAt = computeNextRun(task.getRetryCount());
        task.markFailureAndScheduleRetry(error, nextRunAt);

        Application app = applicationRepository.findById(task.getApplicationId())
                .orElseThrow(() -> new EntityNotFoundException("Application not found: " + task.getApplicationId()));
        if (task.getStatus() == TaskStatus.FAILED) {
            app.markFailed();
        } else {
            app.markPendingForRetry();
        }
    }


    /**
     * 简单指数退避：第1次失败后 2s，第2次 4s，第3次 8s... 上限 60s
     */
    private LocalDateTime computeNextRun(int currentRetryCount) {
        int nextRetry = currentRetryCount + 1;
        long delaySeconds = Math.min(60L, 1L << nextRetry); // 2,4,8,16,32,60...
        return LocalDateTime.now().plusSeconds(delaySeconds);
    }

}
