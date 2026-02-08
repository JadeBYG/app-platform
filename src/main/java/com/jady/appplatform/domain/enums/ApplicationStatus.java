package com.jady.appplatform.domain.enums;

public enum ApplicationStatus {

    PENDING,      // 已接收，尚未处理
    PROCESSING,   // 正在异步处理中
    SUCCESS,      // 处理成功
    FAILED        // 处理失败
}
