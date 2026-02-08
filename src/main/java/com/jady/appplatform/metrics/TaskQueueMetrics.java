package com.jady.appplatform.metrics;

import com.jady.appplatform.repository.ApplicationTaskRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TaskQueueMetrics {

    private final ApplicationTaskRepository repo;
    private final Map<String, Double> statusCounts = new ConcurrentHashMap<>();

    public TaskQueueMetrics(ApplicationTaskRepository repo, MeterRegistry registry) {
        this.repo = repo;

        // 初始化默认值，避免没数据时 gauge 不出现
        statusCounts.put("PENDING", 0.0);
        statusCounts.put("RUNNING", 0.0);
        statusCounts.put("SUCCESS", 0.0);
        statusCounts.put("FAILED", 0.0);

        registerGauge(registry, "PENDING");
        registerGauge(registry, "RUNNING");
        registerGauge(registry, "SUCCESS");
        registerGauge(registry, "FAILED");
    }

    private void registerGauge(MeterRegistry registry, String status) {
        Gauge.builder("app_task_queue_size", statusCounts, m -> m.getOrDefault(status, 0.0))
                .tag("status", status)
                .register(registry);
    }

    @Scheduled(fixedDelay = 5000)
    public void refresh() {
        // 先置零
        statusCounts.put("PENDING", 0.0);
        statusCounts.put("RUNNING", 0.0);
        statusCounts.put("SUCCESS", 0.0);
        statusCounts.put("FAILED", 0.0);

        var rows = repo.countByStatusNative();
        for (Object[] r : rows) {
            String status = String.valueOf(r[0]);
            Number cnt = (Number) r[1];
            statusCounts.put(status, cnt.doubleValue());
        }
    }
}
