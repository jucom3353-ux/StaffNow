package com.example.demo.repository;

import com.example.demo.entity.CompanySubscription;
import com.example.demo.entity.SubscriptionStatus;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanySubscriptionRepository extends JpaRepository<CompanySubscription, Long> {

    Optional<CompanySubscription> findByCompanyAndStatus(User company, SubscriptionStatus status);
    List<CompanySubscription> findByStatusAndExpiredAtBefore(
            SubscriptionStatus status, LocalDateTime now);
    long countByStatus(SubscriptionStatus status);

    @Query("SELECT s FROM CompanySubscription s " +
           "WHERE s.status = :status " +
           "AND s.expiredAt BETWEEN :now AND :soon")
    List<CompanySubscription> findExpiringSoon(
            @Param("status") SubscriptionStatus status,
            @Param("now") LocalDateTime now,
            @Param("soon") LocalDateTime soon);
}