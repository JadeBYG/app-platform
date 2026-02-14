package com.jady.appplatform.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateJobRequest {
    @NotBlank @Size(max = 255)
    public String title;
    @NotBlank @Size(max = 255)
    public String company;

    public String location;
    @NotBlank @Size(max = 5000)
    public String description;
}