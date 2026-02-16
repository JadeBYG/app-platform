package com.jady.appplatform.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateApplicationRequest {

    @NotBlank
    private String requestId;

    @NotNull
    private Long jobId;

    public String getRequestId() {
        return requestId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }
}
