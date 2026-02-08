package com.jady.appplatform.service;

import com.jady.appplatform.repository.ApplicationTaskRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ApplicationTaskReaper {

    private final ApplicationTaskRepository taskRepository;
    private final Counter reclaimedTotal;


    public ApplicationTaskReaper(ApplicationTaskRepository taskRepository, MeterRegistry registry) {
        this.taskRepository = taskRepository;
        this.reclaimedTotal = registry.counter("app_task_reclaimed_total");
    }

    @Scheduled(fixedDelay = 10000) // 每 10 秒扫一次
    @Transactional
    public void reclaimStuckTasks() {
        int released = taskRepository.releaseStuckTasks(30); // 超过 30 秒算卡死
        if (released > 0) {
            reclaimedTotal.increment(released);
            System.out.println("Reclaimed stuck tasks: " + released);
        }
    }
}
