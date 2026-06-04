package com.example.demo.repository;

import com.example.demo.entity.Goal;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    // 현재 진행 중인 목표 (달성 안 된 것)
    Optional<Goal> findByUserAndAchievedFalse(User user);

    // 달성된 목표 포함 전체 목표 조회
    java.util.List<Goal> findByUserOrderByCreatedAtDesc(User user);
}