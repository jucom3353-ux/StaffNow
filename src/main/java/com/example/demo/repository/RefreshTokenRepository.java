package com.example.demo.repository;

import com.example.demo.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByRefreshToken(String refreshToken);
    Optional<RefreshToken> findByUserId(Long userId);

    // ✅ 만료 토큰 일괄 삭제 (스케줄러용)
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiredAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}