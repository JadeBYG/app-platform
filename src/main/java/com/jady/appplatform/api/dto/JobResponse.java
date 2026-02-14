package com.jady.appplatform.api.dto;

import com.jady.appplatform.domain.entity.Job;

public class JobResponse {
    public Long id;
    public String title;
    public String company;
    public String location;
    public String description;
    public String status;
    public Long ownerId;

    public static JobResponse from(Job j) {
        JobResponse r = new JobResponse();
        r.id = j.getId();
        r.title = j.getTitle();
        r.company = j.getCompany();
        r.location = j.getLocation();
        r.description = j.getDescription();
        r.status = j.getStatus().name();
        r.ownerId = j.getOwner().getId();
        return r;
    }
}