package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.EarlyBird;


public interface EarlyBirdRepository extends JpaRepository<EarlyBird, Long> {
    boolean existsByEmail(String email);
    List<EarlyBird> findByMarketingAgreedTrue();
    long countByMarketingAgreedTrue();
}