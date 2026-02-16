package com.jady.appplatform.api.controller;

import com.jady.appplatform.api.dto.CreateApplicationRequest;
import com.jady.appplatform.api.response.ApiResponse;
import com.jady.appplatform.domain.entity.Application;
import com.jady.appplatform.security.SecurityUtil;
import com.jady.appplatform.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Long> create(@Valid @RequestBody CreateApplicationRequest req) {
        Long userId = SecurityUtil.currentUserIdOrThrow();
        Application app = applicationService.createApplication(
                req.getRequestId(),
                userId,
                req.getJobId()
        );
        return ApiResponse.success(app.getId());
    }

}
