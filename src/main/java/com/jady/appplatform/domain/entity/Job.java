package com.jady.appplatform.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "jobs")
public class Job extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    private String location;

    protected Job() {
    }

    public Job(String title, String company, String location) {
        this.title = title;
        this.company = company;
        this.location = location;
    }
}
