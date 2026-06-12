package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.CompanyInviteCode;
import com.example.demo.entity.User;


public interface CompanyInviteCodeRepository extends JpaRepository<CompanyInviteCode, Long> {
    Optional<CompanyInviteCode> findByCode(String code);
    boolean existsByCode(String code);
    List<CompanyInviteCode> findByCompany(User company);
}