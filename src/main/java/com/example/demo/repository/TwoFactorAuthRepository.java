package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.TwoFactorAuth;
import com.example.demo.entity.User;


public interface TwoFactorAuthRepository extends JpaRepository<TwoFactorAuth, Long> {

    Optional<TwoFactorAuth> findTopByUserAndVerifiedFalseOrderByCreatedAtDesc(User user);

    @Modifying
    @Query("DELETE FROM TwoFactorAuth t WHERE t.user = :user")
    void deleteByUser(@Param("user") User user);

    // 만료됐거나 인증 완료된 레코드 일괄 삭제
    @Modifying
    @Query("DELETE FROM TwoFactorAuth t WHERE t.expiredAt < :now OR t.verified = true")
    int deleteExpiredOrVerified(@Param("now") LocalDateTime now);
}