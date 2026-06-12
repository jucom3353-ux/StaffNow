package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.ProfileBoost;
import com.example.demo.entity.User;


public interface ProfileBoostRepository extends JpaRepository<ProfileBoost, Long> {

    // 현재 활성 부스트 조회
    @Query("SELECT p FROM ProfileBoost p WHERE p.user = :user " +
           "AND p.isActive = true " +
           "AND p.startAt <= :now AND p.endAt >= :now")
    Optional<ProfileBoost> findActiveBoost(
            @Param("user") User user,
            @Param("now") LocalDateTime now);

    // 부스트된 유저 ID 목록 (검색 상위 노출용)
    @Query("SELECT p.user.id FROM ProfileBoost p " +
           "WHERE p.isActive = true " +
           "AND p.startAt <= :now AND p.endAt >= :now")
    List<Long> findBoostedUserIds(@Param("now") LocalDateTime now);

    List<ProfileBoost> findByUser(User user);

    // 만료된 활성 부스트 조회 (Scheduler용)
    @Query("SELECT p FROM ProfileBoost p " +
       "WHERE p.isActive = true " +
       "AND p.endAt < :now")
    List<ProfileBoost> findExpiredActiveBoosts(@Param("now") LocalDateTime now);
}