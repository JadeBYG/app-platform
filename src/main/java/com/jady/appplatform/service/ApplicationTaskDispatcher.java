package com.jady.appplatform.service;

import com.jady.appplatform.domain.entity.ApplicationTask;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.List;

@Component
@EnableScheduling
public class ApplicationTaskDispatcher {

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

        List<ApplicationTask> tasks = claimService.claimRunnableTasks(10, workerId);
        claimBatchSize.record(tasks.size());

        for (ApplicationTask task : tasks) {
            executor.submit(() -> {
                try {
                    processor.process(task.getApplicationId());
                    taskService.markSuccess(task.getId());
                    taskSuccess.increment();
                } catch (Exception e) {
                    taskService.markFailureAndScheduleRetry(task.getId(), e.getMessage());
                    taskFailed.increment();
                }
            });
        }
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
