package com.jady.appplatform.repository;

import com.jady.appplatform.domain.entity.Application;
import com.jady.appplatform.domain.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByRequestId(String requestId);

    List<Application> findByStatus(ApplicationStatus status);
}
