package com.jady.appplatform.service;

import org.springframework.stereotype.Component;

@Component
public class ApplicationTaskProcessor {

    /**
     * 这里先放模拟逻辑：后面你可以替换成真正的业务处理，比如：
     * - 更新 applications.status = PROCESSING/SUCCESS/FAILED
     * - 调用外部系统
     * - 写审计日志
     */
    public void process(Long applicationId) throws Exception {
        // 模拟耗时
        Thread.sleep(60000);

        // 模拟偶发失败（用于验证 retry）
        if (applicationId % 5 == 0) {
            throw new RuntimeException("Simulated downstream error for applicationId=" + applicationId);
        }
    }
}
