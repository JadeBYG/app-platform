package com.jady.appplatform.repository;

import com.jady.appplatform.domain.entity.Application;
import com.jady.appplatform.domain.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByRequestId(String requestId);

    List<Application> findByStatus(ApplicationStatus status);

    Page<Application> findByUserId(Long userId, Pageable pageable);

    @Modifying
    @Query(value = """
        UPDATE applications
        SET status = 'PROCESSING'
        WHERE id IN (:ids)
          AND status = 'PENDING'
        """, nativeQuery = true)
    int markProcessingBatch(@Param("ids") List<Long> ids);

    @Query(value = """
        SELECT status, COUNT(*) as cnt
        FROM applications
        GROUP BY status
        """, nativeQuery = true)
    List<Object[]> countByStatusNative();
}
