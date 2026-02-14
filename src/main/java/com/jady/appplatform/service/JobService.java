package com.jady.appplatform.service;

import com.jady.appplatform.api.dto.CreateJobRequest;
import com.jady.appplatform.common.exception.ResourceNotFoundException;
import com.jady.appplatform.domain.entity.Job;
import com.jady.appplatform.domain.entity.User;
import com.jady.appplatform.domain.enums.JobStatus;
import com.jady.appplatform.repository.JobRepository;
import com.jady.appplatform.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public JobService(JobRepository jobRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    public Job createJob(Long ownerId, CreateJobRequest req) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + ownerId));

        Job job = new Job(req.title, req.company, req.location, req.description, owner);
        return jobRepository.save(job);
    }

    public Page<Job> listJobs(String status, Pageable pageable) {
        if (status == null || status.isBlank()) {
            return jobRepository.findAll(pageable);
        }
        JobStatus s = JobStatus.valueOf(status.toUpperCase());
        return jobRepository.findByStatus(s, pageable);
    }
}