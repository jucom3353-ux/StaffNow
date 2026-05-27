package com.example.demo.repository;

import com.example.demo.entity.CompanyInviteCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyInviteCodeRepository extends JpaRepository<CompanyInviteCode, Long> {
    Optional<CompanyInviteCode> findByCode(String code);
    boolean existsByCode(String code);
}