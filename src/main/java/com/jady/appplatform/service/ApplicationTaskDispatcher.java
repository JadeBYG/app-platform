package com.jady.appplatform.service;

import com.jady.appplatform.service.dto.ClaimedTask;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.List;

@Component
@EnableScheduling
public class ApplicationTaskDispatcher {
    private static final Logger log = LoggerFactory.getLogger(ApplicationTaskDispatcher.class);

    private final ApplicationTaskClaimService claimService;
    private final ApplicationTaskService taskService;
    private final ApplicationTaskProcessor processor;
    private final ThreadPoolTaskExecutor executor;

    private final String workerId;

    // ✅ metrics
    private final Counter dispatchTick;
    private final Counter taskSuccess;
    private final Counter taskFailed;
    private final DistributionSummary claimBatchSize;

    public ApplicationTaskDispatcher(
            ApplicationTaskClaimService claimService,
            ApplicationTaskService taskService,
            ApplicationTaskProcessor processor,
            ThreadPoolTaskExecutor taskExecutor,
            Environment env,
            MeterRegistry registry
    ) {
        this.claimService = claimService;
        this.taskService = taskService;
        this.processor = processor;
        this.executor = taskExecutor;
        this.workerId = resolveWorkerId(env);

        // 计数：每次调度 tick
        this.dispatchTick = registry.counter("app_task_dispatch_tick_total", "worker", workerId);

        // 计数：处理成功/失败
        this.taskSuccess = registry.counter("app_task_processed_total", "result", "success", "worker", workerId);
        this.taskFailed  = registry.counter("app_task_processed_total", "result", "failed",  "worker", workerId);

        // 分布：每次 claim 拿了多少条任务（用于观察吞吐与负载均衡）
        this.claimBatchSize = DistributionSummary.builder("app_task_claim_batch_size")
                .tag("worker", workerId)
                .publishPercentileHistogram()
                .register(registry);
    }

    @Scheduled(fixedDelay = 1000)
    public void dispatch() {
        dispatchTick.increment();

        List<ClaimedTask> tasks = claimService.claimRunnableTasks(10, workerId);
        claimBatchSize.record(tasks.size());
        if (!tasks.isEmpty()) {
            log.info("task_dispatch_claimed workerId={} batchSize={}", workerId, tasks.size());
        }

        for (ClaimedTask task : tasks) {
            executor.submit(() -> {
                long startedAt = System.currentTimeMillis();
                try {
                    log.info("task_process_start taskId={} applicationId={} workerId={}",
                            task.taskId(), task.applicationId(), workerId);
                    processor.process(task.applicationId());
                    taskService.markSuccess(task.taskId());
                    taskSuccess.increment();
                    log.info("task_process_success taskId={} applicationId={} workerId={} durationMs={}",
                            task.taskId(), task.applicationId(), workerId, System.currentTimeMillis() - startedAt);
                } catch (Exception e) {
                    taskService.markFailureAndScheduleRetry(task.taskId(), e.getMessage());
                    taskFailed.increment();
                    log.warn("task_process_failure taskId={} applicationId={} workerId={} durationMs={} error={}",
                            task.taskId(), task.applicationId(), workerId,
                            System.currentTimeMillis() - startedAt, sanitizeError(e.getMessage()));
                }
            });
        }
    }

    private String sanitizeError(String message) {
        if (message == null || message.isBlank()) {
            return "unknown";
        }
        return message.length() > 300 ? message.substring(0, 300) : message;
    }

    private String resolveWorkerId(Environment env) {
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            host = "local";
        }
        String port = env.getProperty("server.port", "8080");
        return host + ":" + port;
    }
}
