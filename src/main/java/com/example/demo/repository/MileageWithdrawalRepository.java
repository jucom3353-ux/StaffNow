package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.MileageWithdrawal;
import com.example.demo.entity.MileageWithdrawalStatus;
import com.example.demo.entity.User;


public interface MileageWithdrawalRepository extends JpaRepository<MileageWithdrawal, Long> {
    List<MileageWithdrawal> findByUserOrderByCreatedAtDesc(User user);
    List<MileageWithdrawal> findByStatus(MileageWithdrawalStatus status);
    boolean existsByUserAndStatus(User user, MileageWithdrawalStatus status);
    List<MileageWithdrawal> findAllByOrderByCreatedAtDesc();
    @Query("SELECT COALESCE(SUM(w.requestAmount), 0) FROM MileageWithdrawal w WHERE w.status = :status")
    long sumAmountByStatus(@Param("status") MileageWithdrawalStatus status);
}