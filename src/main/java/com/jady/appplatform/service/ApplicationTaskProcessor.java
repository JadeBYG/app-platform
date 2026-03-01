package com.jady.appplatform.service;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ApplicationTaskProcessor {
    private static final Logger log = LoggerFactory.getLogger(ApplicationTaskProcessor.class);

    /**
     * 这里先放模拟逻辑：后面你可以替换成真正的业务处理，比如：
     * - 更新 applications.status = PROCESSING/SUCCESS/FAILED
     * - 调用外部系统
     * - 写审计日志
     */
    public void process(Long applicationId) throws Exception {
        log.info("application_process_begin applicationId={}", applicationId);
        // 模拟耗时
        Thread.sleep(2000);

        // 模拟偶发失败（用于验证 retry）
        if (applicationId % 5 == 0) {
            throw new RuntimeException("Simulated downstream error for applicationId=" + applicationId);
        }
        log.info("application_process_end applicationId={} result=success", applicationId);
    }
}
