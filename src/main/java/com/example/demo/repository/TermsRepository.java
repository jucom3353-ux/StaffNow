package com.example.demo.repository;

import com.example.demo.entity.Terms;
import com.example.demo.entity.TermsType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TermsRepository extends JpaRepository<Terms, Long> {

    Optional<Terms> findTopByTypeAndIsActiveTrueOrderByCreatedAtDesc(TermsType type);
    List<Terms> findByTypeOrderByCreatedAtDesc(TermsType type);
}