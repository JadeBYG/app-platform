package com.jady.appplatform.service;

import com.jady.appplatform.common.exception.ResourceNotFoundException;
import com.jady.appplatform.domain.entity.Application;
import com.jady.appplatform.domain.entity.ApplicationTask;
import com.jady.appplatform.domain.entity.Job;
import com.jady.appplatform.domain.entity.User;
import com.jady.appplatform.repository.ApplicationRepository;
import com.jady.appplatform.repository.JobRepository;
import com.jady.appplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationTaskService taskService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              UserRepository userRepository,
                              JobRepository jobRepository,
                              ApplicationTaskService taskService) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.taskService = taskService;
    }

    /**
     * 幂等创建：同一个 requestId 多次调用，返回同一条 application（不会重复插入）
     */
    @Transactional
    public Application createApplication(String requestId, Long userId, Long jobId) {
        Application result;
        // 1) 幂等：先查
        var existing = applicationRepository.findByRequestId(requestId);
        if (existing.isPresent()) {
            result = existing.get();
            // 即使是幂等命中，也保证有 task（taskService 内部也是幂等）
            taskService.createTaskIfAbsent(result.getId());
            return result;
        }

        // 2) 校验关联资源存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));

        // 3) 创建并保存
        try {
            Application app = new Application(requestId, user, job);
            result = applicationRepository.save(app);

        } catch (Exception ex) {
            // 2) 并发下唯一约束兜底
            var retry = applicationRepository.findByRequestId(requestId);
            if (retry.isPresent()) {
                result = retry.get();
            } else {
                throw ex;
            }
        }

        // 🔥 新增：创建异步任务（幂等）
        taskService.createTaskIfAbsent(result.getId());
        return result;
    }
}
