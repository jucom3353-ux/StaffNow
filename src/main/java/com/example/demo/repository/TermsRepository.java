package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Terms;
import com.example.demo.entity.TermsType;


public interface TermsRepository extends JpaRepository<Terms, Long> {

    Optional<Terms> findTopByTypeAndIsActiveTrueOrderByCreatedAtDesc(TermsType type);
    List<Terms> findByTypeOrderByCreatedAtDesc(TermsType type);
}