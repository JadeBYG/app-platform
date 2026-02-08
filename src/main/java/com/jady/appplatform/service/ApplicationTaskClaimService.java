package com.jady.appplatform.service;

import com.jady.appplatform.domain.entity.ApplicationTask;
import com.jady.appplatform.repository.ApplicationTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationTaskClaimService {

    private final ApplicationTaskRepository taskRepository;

    public ApplicationTaskClaimService(ApplicationTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public List<ApplicationTask> claimRunnableTasks(int limit, String workerId) {
        // 1) 锁定（SKIP LOCKED：并发时不同 worker 会拿到不同的行）
        List<ApplicationTask> tasks = taskRepository.lockRunnableTasks(limit);

        if (tasks.isEmpty()) {
            return tasks;
        }

        // 2) 在同一个事务里标记 RUNNING
        List<Long> ids = tasks.stream().map(ApplicationTask::getId).toList();
        taskRepository.markRunningBatch(ids, workerId);

        // 注意：这里返回 tasks（它们在 DB 已经被标记 RUNNING）
        return tasks;
    }
}
