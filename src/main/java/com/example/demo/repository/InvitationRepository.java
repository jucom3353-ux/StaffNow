package com.example.demo.repository;

import com.example.demo.entity.Invitation;
import com.example.demo.entity.InvitationStatus;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    List<Invitation> findByWorker(User worker);

    List<Invitation> findByCompany(User company);

    List<Invitation> findByWorkerAndStatus(User worker, InvitationStatus status);

    boolean existsByCompanyAndWorkerAndJobPost(User company, User worker, JobPost jobPost);

    @Modifying
    @Query("UPDATE Invitation i SET i.status = 'EXPIRED', i.updatedAt = CURRENT_TIMESTAMP WHERE i.status = 'PENDING' AND i.createdAt < :expiredTime")
    void expireOldInvitations(@Param("expiredTime") LocalDateTime expiredTime);
    long countByCompany(User company);
}