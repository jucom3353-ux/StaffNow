package com.example.demo.repository;

import com.example.demo.entity.EarlyBird;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EarlyBirdRepository extends JpaRepository<EarlyBird, Long> {
    boolean existsByEmail(String email);
    List<EarlyBird> findByMarketingAgreedTrue();
    long countByMarketingAgreedTrue();
}