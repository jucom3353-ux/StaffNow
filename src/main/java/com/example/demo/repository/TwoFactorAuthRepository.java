package com.example.demo.repository;

import com.example.demo.entity.TwoFactorAuth;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TwoFactorAuthRepository extends JpaRepository<TwoFactorAuth, Long> {

    Optional<TwoFactorAuth> findTopByUserOrderByCreatedAtDesc(User user);

    @Modifying
    @Query("DELETE FROM TwoFactorAuth t WHERE t.user = :user")
    void deleteByUser(@Param("user") User user);
}