package com.jady.appplatform.service;

import com.jady.appplatform.domain.entity.ApplicationTask;
import com.jady.appplatform.domain.enums.TaskStatus;
import com.jady.appplatform.repository.ApplicationTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ApplicationTaskService {

    private final ApplicationTaskRepository taskRepository;

    public ApplicationTaskService(ApplicationTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * 幂等创建：一个 applicationId 只会有一个 task（依赖 DB unique）
     */
    @Transactional
    public ApplicationTask createTaskIfAbsent(Long applicationId) {
        Optional<ApplicationTask> existing = taskRepository.findByApplicationId(applicationId);
        if (existing.isPresent()) {
            return existing.get();
        }
        ApplicationTask task = new ApplicationTask(applicationId, 5, LocalDateTime.now());
        return taskRepository.save(task);
    }

    @Transactional
    public void markRunning(Long taskId, String workerId) {
        ApplicationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        // 保护：只有 PENDING 才能进入 RUNNING（防止重复执行）
        if (task.getStatus() != TaskStatus.PENDING) {
            return;
        }
        task.markRunning(workerId, LocalDateTime.now());
        taskRepository.save(task);
    }

    @Transactional
    public void markSuccess(Long taskId) {
        ApplicationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
        task.markSuccess();
        taskRepository.save(task);
    }

    @Transactional
    public void markFailureAndScheduleRetry(Long taskId, String error) {
        ApplicationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        LocalDateTime next = computeNextRun(task.getRetryCount());
        task.markFailureAndScheduleRetry(error, next);
        taskRepository.save(task);
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
