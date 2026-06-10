package com.example.demo.repository;

import com.example.demo.entity.Payment;
import com.example.demo.entity.PaymentStatus;
import com.example.demo.entity.PaymentType;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByUserOrderByCreatedAtDesc(User user);
    List<Payment> findByUserAndStatusOrderByCreatedAtDesc(User user, PaymentStatus status);
    List<Payment> findByUserAndTypeOrderByCreatedAtDesc(User user, PaymentType type);

    @Query("SELECT p FROM Payment p WHERE p.user = :user " +
           "AND p.createdAt >= :startDate " +
           "ORDER BY p.createdAt DESC")
    List<Payment> findByUserAndMonth(
            @Param("user") User user,
            @Param("startDate") java.time.LocalDateTime startDate);

    // 관리자용
    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID'")
    long sumTotalPaidAmount();

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.user = :user AND p.status = 'PAID'")
    long sumPaidAmountByUser(@Param("user") User user);
}