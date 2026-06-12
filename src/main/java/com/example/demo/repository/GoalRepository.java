package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Goal;
import com.example.demo.entity.User;


public interface GoalRepository extends JpaRepository<Goal, Long> {

    // 현재 진행 중인 목표 (달성 안 된 것)
    Optional<Goal> findByUserAndAchievedFalse(User user);

    // 달성된 목표 포함 전체 목표 조회
    java.util.List<Goal> findByUserOrderByCreatedAtDesc(User user);
}