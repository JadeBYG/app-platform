package com.jady.appplatform.service;

import com.jady.appplatform.common.exception.ResourceNotFoundException;
import com.jady.appplatform.domain.entity.Application;
import com.jady.appplatform.domain.entity.Job;
import com.jady.appplatform.domain.entity.User;
import com.jady.appplatform.repository.ApplicationRepository;
import com.jady.appplatform.repository.JobRepository;
import com.jady.appplatform.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationTaskService taskService;
    private final EntityManager entityManager;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            UserRepository userRepository,
            JobRepository jobRepository,
            ApplicationTaskService taskService,
            EntityManager entityManager
    ) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.taskService = taskService;
        this.entityManager = entityManager;
    }

    /**
     * 幂等创建：同一个 requestId 多次调用，返回同一条 application（不会重复插入）
     */
    @Transactional
    public Application createApplication(String requestId, Long userId, Long jobId) {

        // 1) 幂等：先查
        var existing = applicationRepository.findByRequestId(requestId);
        if (existing.isPresent()) {
            Application app = existing.get();
            taskService.createTaskIfAbsent(app.getId());
            return app;
        }

        // 2) 校验关联资源存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));

        // 3) 创建并保存（并发下可能撞 request_id 唯一键）
        try {
            Application app = new Application(requestId, user, job);
            Application saved = applicationRepository.saveAndFlush(app);
            taskService.createTaskIfAbsent(saved.getId());
            return saved;

        } catch (DataIntegrityViolationException ex) {
            // key: DB constraints exception, clear persistence context
            entityManager.clear();

            // 4) 并发兜底：另一请求已插入成功 → 再查一次返回同一条
            var retry = applicationRepository.findByRequestId(requestId)
                    .orElseThrow(() -> ex);

            taskService.createTaskIfAbsent(retry.getId());
            return retry;
        }
    }
}