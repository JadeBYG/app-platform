package com.jady.appplatform.service;

import com.jady.appplatform.domain.entity.ApplicationTask;
import com.jady.appplatform.repository.ApplicationRepository;
import com.jady.appplatform.repository.ApplicationTaskRepository;
import com.jady.appplatform.service.dto.ClaimedTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationTaskClaimService {
    private static final Logger log = LoggerFactory.getLogger(ApplicationTaskClaimService.class);

    private final ApplicationTaskRepository taskRepository;
    private final ApplicationRepository applicationRepository;

    public ApplicationTaskClaimService(ApplicationTaskRepository taskRepository,
                                       ApplicationRepository applicationRepository) {
        this.taskRepository = taskRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public List<ClaimedTask> claimRunnableTasks(int limit, String workerId) {

        // 1) 锁定：拿到 (id, application_id)
        List<ApplicationTask> rows = taskRepository.lockRunnableTasks(limit);
        if (rows.isEmpty()) return List.of();

        // 2) 提取 ids
        List<Long> ids = rows.stream()
                .map(ApplicationTask::getId)
                .toList();

        // 3) 同事务内标记 RUNNING（DB 层面已经“认领”）
        taskRepository.markRunningBatch(ids, workerId);

        // 4) 对应 application 进入 PROCESSING
        List<Long> appIds = rows.stream()
                .map(ApplicationTask::getApplicationId)
                .distinct()
                .toList();
        applicationRepository.markProcessingBatch(appIds);
        log.info("task_claimed workerId={} limit={} claimed={} applications={}",
                workerId, limit, ids.size(), appIds.size());

        // 5) 返回轻量 DTO，避免 Entity 状态同步问题
        return rows.stream()
                .map(t -> new ClaimedTask(t.getId(), t.getApplicationId()))
                .toList();
    }
}
