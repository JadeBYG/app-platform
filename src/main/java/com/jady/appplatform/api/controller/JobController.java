package com.jady.appplatform.api.controller;

import com.jady.appplatform.api.dto.CreateJobRequest;
import com.jady.appplatform.api.dto.JobResponse;
import com.jady.appplatform.api.response.ApiResponse;
import com.jady.appplatform.security.SecurityUtil;
import com.jady.appplatform.service.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    // 只有 EMPLOYER 可发布
    @PreAuthorize("hasRole('EMPLOYER')")
    @PostMapping
    public ApiResponse<JobResponse> create(@Valid @RequestBody CreateJobRequest req) {
        Long ownerId = SecurityUtil.currentUserIdOrThrow();
        var job = jobService.createJob(ownerId, req);
        return ApiResponse.success(JobResponse.from(job));
    }

    // 浏览：分页 + 可选 status 过滤
    @GetMapping
    public ApiResponse<Page<JobResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status
    ) {
        var p = jobService.listJobs(status, PageRequest.of(page, size))
                .map(JobResponse::from);
        return ApiResponse.success(p);
    }
}