package com.example.demo.repository;

import com.example.demo.entity.CompanyInviteCode;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyInviteCodeRepository extends JpaRepository<CompanyInviteCode, Long> {
    Optional<CompanyInviteCode> findByCode(String code);
    boolean existsByCode(String code);
    List<CompanyInviteCode> findByCompany(User company);
}