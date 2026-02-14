package com.jady.appplatform.domain.entity;

import com.jady.appplatform.domain.enums.JobStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "jobs")
public class Job extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String company;

    @Column(length = 255)
    private String location;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private JobStatus status;

    //Employer
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    protected Job() {
    }

    public Job(String title, String company, String location, String description, User owner) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.owner = owner;
        this.status = JobStatus.OPEN; //default OPEN
    }

    public Long getId() { return super.getId(); }
    public String getTitle() { return title; }
    public String getCompany() { return company; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public JobStatus getStatus() { return status; }
    public User getOwner() { return owner; }

    public void close() { this.status = JobStatus.CLOSED; }
    public void open() { this.status = JobStatus.OPEN; }
}
