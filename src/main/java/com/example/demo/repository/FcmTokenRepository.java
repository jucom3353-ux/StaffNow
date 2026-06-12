package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.FcmToken;
import com.example.demo.entity.User;


public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    List<FcmToken> findByUser(User user);

    Optional<FcmToken> findByToken(String token);

    Optional<FcmToken> findByUserAndToken(User user, String token);

    @Modifying
    @Query("DELETE FROM FcmToken f WHERE f.user = :user")
    void deleteAllByUser(@Param("user") User user);

    @Modifying
    @Query("DELETE FROM FcmToken f WHERE f.token = :token")
    void deleteByToken(@Param("token") String token);
}