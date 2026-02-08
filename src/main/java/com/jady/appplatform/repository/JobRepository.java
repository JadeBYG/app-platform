package com.jady.appplatform.repository;

import com.jady.appplatform.domain.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {
}
