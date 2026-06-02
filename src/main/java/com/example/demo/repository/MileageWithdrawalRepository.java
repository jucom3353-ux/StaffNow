package com.example.demo.repository;

import com.example.demo.entity.MileageWithdrawal;
import com.example.demo.entity.MileageWithdrawalStatus;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MileageWithdrawalRepository extends JpaRepository<MileageWithdrawal, Long> {
    List<MileageWithdrawal> findByUserOrderByCreatedAtDesc(User user);
    List<MileageWithdrawal> findByStatus(MileageWithdrawalStatus status);
    boolean existsByUserAndStatus(User user, MileageWithdrawalStatus status);
    List<MileageWithdrawal> findAllByOrderByCreatedAtDesc();
}