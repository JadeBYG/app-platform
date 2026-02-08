package com.jady.appplatform.api.controller;

import com.jady.appplatform.api.dto.CreateApplicationRequest;
import com.jady.appplatform.api.response.ApiResponse;
import com.jady.appplatform.domain.entity.Application;
import com.jady.appplatform.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody CreateApplicationRequest req) {
        Application app = applicationService.createApplication(
                req.getRequestId(),
                req.getUserId(),
                req.getJobId()
        );
        return ApiResponse.success(app.getId());
    }

}
